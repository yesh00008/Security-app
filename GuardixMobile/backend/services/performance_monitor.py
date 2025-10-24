import random
from typing import Dict


class PerformanceMonitor:
    async def check_performance(self) -> Dict:
        return {
            "cpu": {"usage": round(random.uniform(10.0, 80.0), 1)},
            "memory": {"usage": round(random.uniform(20.0, 90.0), 1)},
            "storage": {"free_gb": round(random.uniform(5.0, 60.0), 1)},
        }

