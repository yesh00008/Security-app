from typing import List, Tuple
from uuid import uuid4

import numpy as np
from sklearn.ensemble import IsolationForest

from app.core.config import settings


PROFILE = settings.resolve_profile(None)


class IDSService:
    def __init__(self) -> None:
        # Tiny baseline model with synthetic "normal" traffic
        normal = np.array([
            [1000, 900, 5, 0],
            [1500, 1400, 6, 0],
            [2000, 1800, 8, 0],
            [800, 700, 4, 0],
            [2200, 2000, 8, 1],
        ], dtype=float)
        contamination = 0.12 if PROFILE == "lite" else 0.08
        n_estimators = 40 if PROFILE == "lite" else 80
        self.model = IsolationForest(contamination=contamination, random_state=42, n_estimators=n_estimators, warm_start=True)
        self.profile = PROFILE
        self.algorithm = "IsolationForest"
        self.model.fit(normal)

    def new_session_id(self) -> str:
        return f"ids_{uuid4()}"

    def score(self, traffic: List[dict]) -> Tuple[bool, float, List[dict]]:
        if not traffic:
            return False, 0.0, []
        X = np.array([[t.get("bytes_in", 0), t.get("bytes_out", 0), t.get("connections", 0), t.get("failed_auth", 0)] for t in traffic], dtype=float)
        scores = -self.model.score_samples(X)  # higher => more anomalous
        score = float(np.clip(scores.mean() / 10.0, 0.0, 1.0))
        alert_threshold = 0.55 if self.profile == "lite" else 0.6
        alert = bool(score > alert_threshold)
        anomalies = []
        for i, s in enumerate(scores):
            if s > np.percentile(scores, 80):
                anomalies.append({"index": i, "score": float(s), "record": traffic[i]})
        return alert, score, anomalies

    def info(self) -> dict:
        return {
            "name": "ids",
            "algorithm": self.algorithm,
            "profile": self.profile,
            "model_path": None,
        }


ids_service = IDSService()
