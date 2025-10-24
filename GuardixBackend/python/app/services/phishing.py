import os
from typing import Tuple
from uuid import uuid4

import joblib
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression, SGDClassifier
from sklearn.pipeline import make_pipeline

from app.core.config import settings


PROFILE = settings.resolve_profile(None)
MODEL_PATH = os.path.join(settings.models_dir, f"phishing_{PROFILE}.joblib")


def _default_corpus():
    X = [
        "Verify your account now http://phish.me/login",
        "Update password required urgent",
        "Congratulations you won click here",
        "Meeting at 10am agenda attached",
        "Company newsletter September",
        "Your receipt from app store",
    ]
    y = [1, 1, 1, 0, 0, 0]
    return X, y


class PhishingService:
    def __init__(self) -> None:
        self.model = None
        self.profile = PROFILE
        self.algorithm = "SGDClassifier" if self.profile == "lite" else "LogisticRegression"
        self._ensure_model()

    def _ensure_model(self):
        if os.path.exists(MODEL_PATH):
            self.model = joblib.load(MODEL_PATH)
            return
        X, y = _default_corpus()
        vectorizer_kwargs = {
            "ngram_range": (1, 1 if self.profile == "lite" else 2),
            "min_df": 1,
            "max_features": 300 if self.profile == "lite" else 1200,
        }
        if self.profile == "lite":
            classifier = SGDClassifier(loss="log_loss", max_iter=200, tol=1e-3, random_state=42)
        else:
            classifier = LogisticRegression(max_iter=400)
        self.model = make_pipeline(TfidfVectorizer(**vectorizer_kwargs), classifier)
        self.model.fit(X, y)
        joblib.dump(self.model, MODEL_PATH)

    def score(self, url: str | None, text: str | None) -> float:
        content = text or url or ""
        proba = self.model.predict_proba([content])[0]
        return float(proba[1])

    def new_scan_id(self) -> str:
        return f"phish_{uuid4()}"

    def info(self) -> dict:
        return {
            "name": "phishing",
            "algorithm": self.algorithm,
            "profile": self.profile,
            "model_path": MODEL_PATH,
        }


phishing_service = PhishingService()
