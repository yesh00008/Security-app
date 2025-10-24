import asyncio
import logging
import time
import psutil
import platform
from datetime import datetime
from typing import Dict, Any, List
import socket
import uuid

logger = logging.getLogger(__name__)

class RealtimeDataService:
    """Service that provides real-time system data"""
    
    def __init__(self):
        self.running = False
        self.interval = 1.0  # Default update interval in seconds
        
    async def get_system_metrics(self) -> Dict[str, Any]:
        """Get current system metrics (CPU, memory, disk, network)"""
        cpu_percent = psutil.cpu_percent(interval=0.1)
        memory = psutil.virtual_memory()
        disk = psutil.disk_usage('/')
        net_io = psutil.net_io_counters()
        
        return {
            "type": "system_metrics",
            "data": {
                "cpu": {
                    "usage_percent": cpu_percent,
                    "cores": psutil.cpu_count(logical=True),
                    "physical_cores": psutil.cpu_count(logical=False)
                },
                "memory": {
                    "total": memory.total,
                    "available": memory.available,
                    "used": memory.used,
                    "percent": memory.percent
                },
                "disk": {
                    "total": disk.total,
                    "used": disk.used,
                    "free": disk.free,
                    "percent": disk.percent
                },
                "network": {
                    "bytes_sent": net_io.bytes_sent,
                    "bytes_recv": net_io.bytes_recv,
                    "packets_sent": net_io.packets_sent,
                    "packets_recv": net_io.packets_recv
                },
                "timestamp": datetime.utcnow().isoformat()
            }
        }
        
    async def get_network_status(self) -> Dict[str, Any]:
        """Get current network status and connection information"""
        hostname = socket.gethostname()
        
        # Get all network interfaces
        interfaces = []
        for interface, addrs in psutil.net_if_addrs().items():
            addresses = []
            for addr in addrs:
                if addr.family == socket.AF_INET:
                    addresses.append({
                        "ip": addr.address,
                        "netmask": addr.netmask
                    })
            
            if addresses:  # Only add interfaces with IPv4 addresses
                interfaces.append({
                    "name": interface,
                    "addresses": addresses
                })
        
        # Get network statistics
        net_io = psutil.net_io_counters()
        net_stats = {
            "bytes_sent": net_io.bytes_sent,
            "bytes_recv": net_io.bytes_recv,
            "packets_sent": net_io.packets_sent,
            "packets_recv": net_io.packets_recv,
            "errin": net_io.errin,
            "errout": net_io.errout,
            "dropin": net_io.dropin,
            "dropout": net_io.dropout
        }
        
        # Calculate current network speed (requires previous measurements)
        # This is a placeholder - real implementation would track previous values
        download_speed = 5.5 + (time.time() % 10)  # Simulated fluctuation
        upload_speed = 1.2 + (time.time() % 5)     # Simulated fluctuation
        
        return {
            "type": "network_status",
            "data": {
                "hostname": hostname,
                "interfaces": interfaces,
                "statistics": net_stats,
                "current_speeds": {
                    "download_mbps": download_speed,
                    "upload_mbps": upload_speed
                },
                "timestamp": datetime.utcnow().isoformat()
            }
        }
        
    async def get_security_events(self) -> Dict[str, Any]:
        """Get latest security events (simulated but based on real patterns)"""
        # In a real implementation, this would pull from actual security monitoring
        # For now, we'll simulate events based on common patterns
        
        # Generate a simulated event based on time
        event_types = [
            "Suspicious Login Attempt",
            "Unusual Network Traffic",
            "Potential Malware Detected",
            "Permission Change",
            "System File Modified",
            "Firewall Rule Violation"
        ]
        
        severity_levels = ["Low", "Medium", "High", "Critical"]
        
        # Use time-based seed for some variety but predictability
        seed = int(time.time()) % 100
        
        # Simulate 0-3 events
        num_events = min(3, max(0, (seed % 4)))
        events = []
        
        for i in range(num_events):
            event_type = event_types[(seed + i) % len(event_types)]
            severity = severity_levels[(seed + i * 7) % len(severity_levels)]
            
            events.append({
                "id": str(uuid.uuid4()),
                "type": event_type,
                "severity": severity,
                "source": f"192.168.1.{(seed + i * 13) % 255}",
                "timestamp": datetime.utcnow().isoformat()
            })
            
        return {
            "type": "security_events",
            "data": {
                "events": events,
                "total_count": num_events,
                "timestamp": datetime.utcnow().isoformat()
            }
        }
        
    async def get_process_list(self) -> Dict[str, Any]:
        """Get list of running processes with resource usage"""
        processes = []
        
        for proc in psutil.process_iter(['pid', 'name', 'username', 'cpu_percent', 'memory_percent']):
            try:
                pinfo = proc.info
                processes.append({
                    "pid": pinfo['pid'],
                    "name": pinfo['name'],
                    "username": pinfo['username'],
                    "cpu_percent": pinfo['cpu_percent'],
                    "memory_percent": pinfo['memory_percent']
                })
            except (psutil.NoSuchProcess, psutil.AccessDenied, psutil.ZombieProcess):
                pass
                
        # Sort by CPU usage (descending)
        processes.sort(key=lambda x: x['cpu_percent'], reverse=True)
        
        # Take top 10 processes
        top_processes = processes[:10]
        
        return {
            "type": "process_list",
            "data": {
                "processes": top_processes,
                "total_count": len(processes),
                "timestamp": datetime.utcnow().isoformat()
            }
        }
        
    async def start_data_stream(self, callback, interval=1.0):
        """Start streaming data at specified interval"""
        self.running = True
        self.interval = interval
        
        logger.info(f"Starting real-time data stream with interval {interval}s")
        
        while self.running:
            try:
                # Get all metrics
                system_metrics = await self.get_system_metrics()
                await callback(system_metrics)
                
                # Get network status every 2 cycles
                if int(time.time()) % 2 == 0:
                    network_status = await self.get_network_status()
                    await callback(network_status)
                
                # Get security events every 3 cycles
                if int(time.time()) % 3 == 0:
                    security_events = await self.get_security_events()
                    await callback(security_events)
                    
                # Get process list every 5 cycles
                if int(time.time()) % 5 == 0:
                    process_list = await self.get_process_list()
                    await callback(process_list)
                    
                await asyncio.sleep(self.interval)
                
            except Exception as e:
                logger.error(f"Error in data stream: {str(e)}")
                await asyncio.sleep(self.interval)
                
    def stop_data_stream(self):
        """Stop the data stream"""
        self.running = False
        logger.info("Stopping real-time data stream")

# Global instance
realtime_service = RealtimeDataService()