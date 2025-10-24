from pydantic import BaseModel
import os


class Settings(BaseModel):
    app_name: str = "Guardix Security API"
    version: str = "1.0.0"
    environment: str = os.getenv("ENV", "dev")
    model_profile: str = os.getenv("MODEL_PROFILE", "lite")

    # Security
    jwt_secret: str = os.getenv("JWT_SECRET", "dev_secret_change_me")
    jwt_algorithm: str = os.getenv("JWT_ALG", "HS256")
    access_token_exp_minutes: int = int(os.getenv("JWT_EXP_MIN", "60"))

    # Database (MongoDB optional)
    mongo_enabled: bool = os.getenv("MONGO_ENABLED", "false").lower() == "true"
    mongo_uri: str = os.getenv("MONGO_URI", "mongodb://localhost:27017")
    mongo_db: str = os.getenv("MONGO_DB", "guardix")

    # Paths
    base_dir: str = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
    models_dir: str = os.path.join(base_dir, "models_store")
    data_dir: str = os.path.join(base_dir, "data")

    def resolve_profile(self, profile: str | None = None) -> str:
        value = (profile or self.model_profile or "lite").lower()
        return value if value in {"lite", "standard"} else "lite"


settings = Settings()

os.makedirs(settings.models_dir, exist_ok=True)
os.makedirs(settings.data_dir, exist_ok=True)
