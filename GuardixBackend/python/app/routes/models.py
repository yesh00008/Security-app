from fastapi import APIRouter, Depends

from app.core.config import settings
from app.core.security import get_current_user
from app.services.model_registry import list_models


router = APIRouter(prefix="/models", tags=["models"])


@router.get("/", dependencies=[Depends(get_current_user)])
async def models_summary():
    return {
        "active_profile": settings.resolve_profile(None),
        "models": list_models(),
    }

