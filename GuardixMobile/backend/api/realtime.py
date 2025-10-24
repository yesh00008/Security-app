from fastapi import APIRouter, WebSocket, WebSocketDisconnect
from datetime import datetime
import asyncio
import psutil
import random

realtime_router = APIRouter()


def _system_metrics_payload():
    vm = psutil.virtual_memory()
    disk = psutil.disk_usage("/")
    net_io = psutil.net_io_counters()
    cpu_percent = psutil.cpu_percent(interval=None)
    return {
        "type": "system_metrics",
        "timestamp": datetime.now().isoformat(),
        "data": {
            "cpu": {
                "usage_percent": float(cpu_percent),
                "cores": psutil.cpu_count() or 4,
                "physical_cores": psutil.cpu_count(logical=False) or 2,
            },
            "memory": {
                "total": int(vm.total // (1024 * 1024)),
                "available": int(vm.available // (1024 * 1024)),
                "used": int(vm.used // (1024 * 1024)),
                "percent": float(vm.percent),
            },
            "disk": {
                "total": int(disk.total // (1024 * 1024)),
                "used": int(disk.used // (1024 * 1024)),
                "free": int(disk.free // (1024 * 1024)),
                "percent": float(disk.percent),
            },
            "network": {
                "bytes_sent": int(net_io.bytes_sent),
                "bytes_recv": int(net_io.bytes_recv),
                "packets_sent": int(net_io.packets_sent),
                "packets_recv": int(net_io.packets_recv),
            },
            "timestamp": datetime.now().isoformat(),
        },
    }


def _network_status_payload():
    # Minimal mock data; building actual interfaces list cross-platform is non-trivial
    return {
        "type": "network_status",
        "timestamp": datetime.now().isoformat(),
        "data": {
            "hostname": "guardix-local",
            "interfaces": [
                {
                    "name": "eth0",
                    "addresses": [{"ip": "192.168.1.100", "netmask": "255.255.255.0"}],
                }
            ],
            "statistics": {
                "bytes_sent": random.randint(10_000, 10_000_000),
                "bytes_recv": random.randint(10_000, 10_000_000),
                "packets_sent": random.randint(100, 100_000),
                "packets_recv": random.randint(100, 100_000),
                "errin": 0,
                "errout": 0,
                "dropin": 0,
                "dropout": 0,
            },
            "current_speeds": {
                "download_mbps": round(random.uniform(5.0, 120.0), 1),
                "upload_mbps": round(random.uniform(1.0, 40.0), 1),
            },
            "timestamp": datetime.now().isoformat(),
        },
    }


def _security_events_payload():
    return {
        "type": "security_events",
        "timestamp": datetime.now().isoformat(),
        "data": {
            "events": [
                {
                    "id": f"evt_{random.randint(1000,9999)}",
                    "type": random.choice(["scan_completed", "threat_blocked", "anomaly_detected"]),
                    "severity": random.choice(["low", "medium", "high"]),
                    "source": random.choice(["scanner", "network", "monitor"]),
                    "timestamp": datetime.now().isoformat(),
                }
            ],
            "total_count": random.randint(1, 500),
            "timestamp": datetime.now().isoformat(),
        },
    }


def _process_list_payload():
    # Limit to a few processes for payload size
    procs = []
    count = 0
    for p in psutil.process_iter(attrs=["pid", "name", "username"]):
        if count >= 5:
            break
        try:
            cpu = p.cpu_percent(interval=None) / max(1, psutil.cpu_count())
            mem = p.memory_percent()
            procs.append(
                {
                    "pid": p.info.get("pid", 0),
                    "name": p.info.get("name", "proc"),
                    "username": p.info.get("username", "user"),
                    "cpu_percent": round(float(cpu), 2),
                    "memory_percent": round(float(mem), 2),
                }
            )
            count += 1
        except Exception:
            continue
    return {
        "type": "process_list",
        "timestamp": datetime.now().isoformat(),
        "data": {
            "processes": procs,
            "total_count": len(procs),
            "timestamp": datetime.now().isoformat(),
        },
    }


async def _send_safe(ws: WebSocket, payload: dict) -> bool:
    try:
        await ws.send_json(payload)
        return True
    except Exception:
        return False


@realtime_router.websocket("/realtime/ws")
async def realtime_ws(websocket: WebSocket):
    await websocket.accept()
    try:
        # Send an initial burst of messages
        for _ in range(3):
            if not (_send := await _send_safe(websocket, _system_metrics_payload())):
                break
            if not (_send := await _send_safe(websocket, _network_status_payload())):
                break
            if not (_send := await _send_safe(websocket, _security_events_payload())):
                break
            if not (_send := await _send_safe(websocket, _process_list_payload())):
                break
            await asyncio.sleep(1.0)

        # Keep streaming system metrics periodically
        while True:
            if not await _send_safe(websocket, _system_metrics_payload()):
                break
            await asyncio.sleep(2.0)
    except WebSocketDisconnect:
        return
