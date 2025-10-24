"""Simple in-memory storage as a fallback when no DB is configured."""
from datetime import datetime
from typing import Any, Dict, List
from uuid import uuid4


class MemoryStore:
    def __init__(self) -> None:
        self.users: Dict[str, Dict[str, Any]] = {}
        self.logs: List[Dict[str, Any]] = []
        self.alerts: List[Dict[str, Any]] = []
        self.biometric_profiles: Dict[str, Dict[str, Any]] = {}

    # Users
    def upsert_user(self, user_id: str, data: Dict[str, Any]) -> None:
        self.users[user_id] = {**self.users.get(user_id, {}), **data}

    def get_user(self, user_id: str) -> Dict[str, Any]:
        return self.users.get(user_id)

    # Logs
    def add_log(self, entry: Dict[str, Any]) -> str:
        entry = {**entry, "_id": str(uuid4()), "ts": datetime.utcnow().isoformat()}
        self.logs.append(entry)
        return entry["_id"]

    # Alerts
    def add_alert(self, entry: Dict[str, Any]) -> str:
        entry = {**entry, "_id": str(uuid4()), "ts": datetime.utcnow().isoformat()}
        self.alerts.append(entry)
        return entry["_id"]

    # Biometric Profiles
    def set_biometric_profile(self, user_id: str, profile: Dict[str, Any]) -> None:
        self.biometric_profiles[user_id] = profile

    def get_biometric_profile(self, user_id: str) -> Dict[str, Any]:
        return self.biometric_profiles.get(user_id)


memdb = MemoryStore()

