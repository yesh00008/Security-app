import random
from typing import List, Dict


class AnomalyDetector:
    async def detect_anomalies(self) -> List[Dict]:
        anomalies = [
            {"subsystem": "cpu", "score": round(random.uniform(0.2, 0.9), 2)},
            {"subsystem": "io", "score": round(random.uniform(0.2, 0.9), 2)},
        ]
        return anomalies[: random.randint(0, len(anomalies))]

