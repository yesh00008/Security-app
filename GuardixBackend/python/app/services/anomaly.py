import os
from typing import Tuple

import joblib
import numpy as np
from sklearn.svm import OneClassSVM
from sklearn.neighbors import LocalOutlierFactor

from app.core.config import settings


PROFILE = settings.resolve_profile(None)
BEHAVIOR_MODEL_PATH = os.path.join(settings.models_dir, f"behavior_anomaly_{PROFILE}.joblib")
SYSTEM_MODEL_PATH = os.path.join(settings.models_dir, f"system_anomaly_{PROFILE}.joblib")


class BehaviorAnomalyService:
    def __init__(self) -> None:
        self.profile = PROFILE
        self.algorithm = "OneClassSVM"
        self.model = None
        self._ensure_model()

    def _ensure_model(self) -> None:
        if os.path.exists(BEHAVIOR_MODEL_PATH):
            self.model = joblib.load(BEHAVIOR_MODEL_PATH)
            return

        rng = np.random.default_rng(42)
        # Synthetic normal usage metrics: cpu, memory, network, battery drain, touch cadence
        normal = np.column_stack(
            [
                rng.normal(45, 10, 400),  # CPU usage
                rng.normal(55, 12, 400),  # Memory usage
                rng.normal(30, 8, 400),   # Network utilisation
                rng.normal(8, 2, 400),    # Battery drain
                rng.normal(120, 20, 400), # Interaction cadence
            ]
        )

        self.model = OneClassSVM(kernel="rbf", gamma="scale", nu=0.05)
        self.model.fit(normal)
        joblib.dump(self.model, BEHAVIOR_MODEL_PATH)

    def score(self, metrics: list[float]) -> Tuple[str, float, float]:
        arr = np.array(metrics, dtype=float).reshape(1, -1)
        raw = float(self.model.decision_function(arr)[0])
        probability = float(1.0 / (1.0 + np.exp(-raw)))
        label = "anomaly" if raw < 0 else "normal"
        return label, raw, probability

    def info(self) -> dict:
        return {
            "name": "behavior_anomaly",
            "algorithm": self.algorithm,
            "profile": self.profile,
            "model_path": BEHAVIOR_MODEL_PATH,
        }


class SystemAnomalyService:
    def __init__(self) -> None:
        self.profile = PROFILE
        self.algorithm = "LocalOutlierFactor"
        self.model = None
        self._ensure_model()

    def _ensure_model(self) -> None:
        if os.path.exists(SYSTEM_MODEL_PATH):
            self.model = joblib.load(SYSTEM_MODEL_PATH)
            return

        rng = np.random.default_rng(21)
        baseline = np.column_stack(
            [
                rng.normal(36, 4, 300),   # Temperature
                rng.normal(12, 3, 300),   # CPU load variability
                rng.normal(80, 10, 300),  # Disk throughput
                rng.normal(0.5, 0.2, 300) # Error rate
            ]
        )

        model = LocalOutlierFactor(n_neighbors=20, novelty=True, contamination=0.08)
        model.fit(baseline)
        joblib.dump(model, SYSTEM_MODEL_PATH)
        self.model = model

    def score(self, metrics: list[float]) -> Tuple[str, float, float]:
        arr = np.array(metrics, dtype=float).reshape(1, -1)
        raw = float(self.model.decision_function(arr)[0])
        probability = float(1.0 / (1.0 + np.exp(-raw)))
        label = "anomaly" if raw < 0 else "normal"
        return label, raw, probability

    def info(self) -> dict:
        return {
            "name": "system_anomaly",
            "algorithm": self.algorithm,
            "profile": self.profile,
            "model_path": SYSTEM_MODEL_PATH,
        }


behavior_anomaly_service = BehaviorAnomalyService()
system_anomaly_service = SystemAnomalyService()

