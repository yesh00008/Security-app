import os
from dataclasses import dataclass, asdict
from typing import List

from app.services.malware import malware_service
from app.services.phishing import phishing_service
from app.services.biometric import biometric_service
from app.services.ids import ids_service
from app.services.anomaly import behavior_anomaly_service, system_anomaly_service


@dataclass
class ModelMetadata:
    name: str
    algorithm: str
    profile: str
    size_kb: float | None
    path: str | None


def _file_size_kb(path: str | None) -> float | None:
    if not path or not os.path.exists(path):
        return None
    return round(os.path.getsize(path) / 1024, 2)


def list_models() -> List[dict]:
    registry = []
    for service in (
        malware_service,
        phishing_service,
        biometric_service,
        ids_service,
        behavior_anomaly_service,
        system_anomaly_service,
    ):
        info = service.info()
        metadata = ModelMetadata(
            name=info.get("name"),
            algorithm=info.get("algorithm"),
            profile=info.get("profile"),
            path=info.get("model_path"),
            size_kb=_file_size_kb(info.get("model_path")),
        )
        registry.append(asdict(metadata))
    return registry
