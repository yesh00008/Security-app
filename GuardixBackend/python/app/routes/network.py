from fastapi import APIRouter, Depends
import asyncio
import random

from app.core.security import get_current_user
from app.models.schemas import (
    NetworkSecurityScanRequest,
    NetworkSecurityScanResponse,
    NetworkUsageResponse,
)


router = APIRouter(prefix="/network-tools", tags=["Network Tools"])


@router.post("/security-scan", response_model=NetworkSecurityScanResponse)
async def network_security_scan(
    request: NetworkSecurityScanRequest,
    current_user: dict = Depends(get_current_user),
):
    """Run a simulated network security scan."""
    await asyncio.sleep(2)

    threats = []
    if request.include_wifi and random.random() > 0.6:
        threats.append(
            {
                "type": "WiFi",
                "description": "Detected weak WiFi encryption (WPA)",
                "severity": "medium",
                "recommendation": "Switch to WPA2/WPA3",
            }
        )
    if request.include_firewall and random.random() > 0.7:
        threats.append(
            {
                "type": "Firewall",
                "description": "Inbound port 8080 open",
                "severity": "low",
                "recommendation": "Close unused ports",
            }
        )

    open_ports = sorted(random.sample(range(1024, 1050), k=5))
    wifi_security = {
        "ssid": "GuardixSecure",
        "encryption": random.choice(["WPA2", "WPA3", "Open"]),
        "signal_strength": f"-{random.randint(38, 62)} dBm",
        "channel": random.randint(1, 11),
    }

    recommendations = [
        "Update router firmware",
        "Disable WPS if not required",
        "Use strong WiFi password",
        "Enable firewall intrusion detection",
    ]

    return NetworkSecurityScanResponse(
        threats_detected=threats,
        open_ports=open_ports,
        wifi_security=wifi_security,
        recommendations=recommendations,
    )


@router.get("/usage", response_model=NetworkUsageResponse)
async def network_usage(current_user: dict = Depends(get_current_user)):
    """Return current network usage statistics."""
    await asyncio.sleep(1)

    current_network = {
        "type": random.choice(["WiFi", "5G", "4G"]),
        "ssid": "GuardixSecure",
        "ip_address": f"192.168.0.{random.randint(2, 220)}",
        "signal_strength": f"-{random.randint(38, 58)} dBm",
    }

    usage_stats = {
        "download_today": random.uniform(1.5, 4.5) * 1024 * 1024 * 1024,
        "upload_today": random.uniform(0.5, 1.2) * 1024 * 1024 * 1024,
        "download_speed": random.uniform(40, 180),
        "upload_speed": random.uniform(15, 60),
    }

    top_apps = [
        {
            "name": "Streaming App",
            "package": "com.video.app",
            "download": random.uniform(800, 1500) * 1024 * 1024,
            "upload": random.uniform(50, 120) * 1024 * 1024,
        },
        {
            "name": "Cloud Backup",
            "package": "com.cloud.sync",
            "download": random.uniform(300, 500) * 1024 * 1024,
            "upload": random.uniform(400, 700) * 1024 * 1024,
        },
        {
            "name": "Browser",
            "package": "com.browser.web",
            "download": random.uniform(200, 400) * 1024 * 1024,
            "upload": random.uniform(40, 90) * 1024 * 1024,
        },
    ]

    quality = random.choice(["Excellent", "Good", "Fair"])

    return NetworkUsageResponse(
        current_network=current_network,
        usage_stats=usage_stats,
        top_apps=top_apps,
        connection_quality=quality,
    )

