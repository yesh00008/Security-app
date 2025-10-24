from typing import Tuple
import numpy as np

from app.core.config import settings
from app.db.memory import memdb


def _feature_vector(sample: dict) -> np.ndarray:
    # Simple feature composition; real system would be richer
    kt = np.array(sample.get("keystroke_timings", []) or [0.0])
    tp = np.array(sample.get("touch_pressure", []) or [0.0])
    ti = np.array(sample.get("touch_intervals", []) or [0.0])
    # Aggregate statistics
    feats = [
        kt.mean(), kt.std() if kt.size > 1 else 0.0,
        tp.mean(), tp.std() if tp.size > 1 else 0.0,
        ti.mean(), ti.std() if ti.size > 1 else 0.0,
    ]
    return np.nan_to_num(np.array(feats, dtype=float))


PROFILE = settings.resolve_profile(None)


class BiometricService:
    def __init__(self) -> None:
        self.profile = PROFILE
        self.algorithm = "CosineSimilarity"

    def enroll(self, user_id: str, sample: dict) -> dict:
        vec = _feature_vector(sample)
        profile = {"mean": vec.tolist()}
        memdb.set_biometric_profile(user_id, profile)
        return profile

    def verify(self, user_id: str, sample: dict, threshold: float | None = None) -> Tuple[bool, float, float]:
        if threshold is None:
            threshold = 0.8 if self.profile == "lite" else 0.85
        profile = memdb.get_biometric_profile(user_id)
        if not profile:
            # Auto-enroll on first use for demo
            profile = self.enroll(user_id, sample)
        mean = np.array(profile["mean"])
        vec = _feature_vector(sample)
        # Cosine similarity as a simple proxy
        denom = (np.linalg.norm(mean) * np.linalg.norm(vec)) or 1e-6
        sim = float(np.dot(mean, vec) / denom)
        return sim >= threshold, sim, threshold

    def info(self) -> dict:
        return {
            "name": "biometric",
            "algorithm": self.algorithm,
            "profile": self.profile,
            "model_path": None,
        }


biometric_service = BiometricService()
