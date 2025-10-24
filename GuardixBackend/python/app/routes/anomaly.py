from fastapi import APIRouter, Depends

from app.core.security import get_current_user
from app.models.schemas import (
    BehaviorAnomalyRequest,
    BehaviorAnomalyResponse,
    SystemAnomalyRequest,
    SystemAnomalyResponse,
)
from app.services.anomaly import behavior_anomaly_service, system_anomaly_service


router = APIRouter(prefix="/anomaly", tags=["Anomaly Detection"])


@router.post("/behavior", response_model=BehaviorAnomalyResponse)
async def detect_behavior(request: BehaviorAnomalyRequest, current_user: dict = Depends(get_current_user)):
    label, raw, probability = behavior_anomaly_service.score(request.metrics)
    return BehaviorAnomalyResponse(label=label, score=raw, probability=probability)


@router.post("/system", response_model=SystemAnomalyResponse)
async def detect_system(request: SystemAnomalyRequest, current_user: dict = Depends(get_current_user)):
    label, raw, probability = system_anomaly_service.score(request.metrics)
    return SystemAnomalyResponse(label=label, score=raw, probability=probability)

