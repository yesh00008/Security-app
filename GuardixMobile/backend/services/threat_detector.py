import random
from typing import List, Dict, Any


class ThreatDetector:
    async def quick_scan(self) -> List[Dict[str, Any]]:
        # Return a few sample potential risks
        items = [
            {"type": "tracking_sdk", "package": "com.ads.sdk", "severity": "low"},
            {"type": "permission_overuse", "package": "com.example.app", "severity": "medium"},
        ]
        return items[: random.randint(0, len(items))]

    async def full_scan(self) -> List[Dict,]:
        threats = [
            {"type": "risk_app", "package": "com.unknown.app", "severity": "medium"},
            {"type": "suspicious_behavior", "process": "sh", "severity": "high"},
        ]
        return threats[: random.randint(1, len(threats))]

    async def malware_scan(self) -> List[Dict,]:
        return [
            {"signature": "GEN.MLW.001", "file": "/storage/app/base.apk", "severity": "high"}
        ] if random.random() > 0.6 else []

    async def privacy_audit(self) -> List[Dict,]:
        return [
            {"issue": "camera_background_access", "app": "com.camera.app"},
            {"issue": "contacts_access", "app": "com.social.app"},
        ]

