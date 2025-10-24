import random
from typing import List, Dict


class NetworkAnalyzer:
    async def scan_network(self) -> List[Dict]:
        return [
            {
                "ip": f"192.168.1.{random.randint(2, 200)}",
                "threat": random.choice(["open_port", "weak_password", "none"]),
                "severity": random.choice(["low", "medium", "high"]),
            }
            for _ in range(random.randint(0, 3))
        ]

