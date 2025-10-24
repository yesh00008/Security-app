from datetime import datetime
from fastapi import APIRouter, Depends

from app.core.security import get_current_user
from app.models.schemas import BiometricAuthRequest, BiometricAuthResponse
from app.services.biometric import biometric_service


router = APIRouter(prefix="/auth", tags=["biometric"])


@router.post("/biometric", response_model=BiometricAuthResponse)
async def biometric_auth(req: BiometricAuthRequest, user=Depends(get_current_user)):
    match, prob, threshold = biometric_service.verify(req.user_id, req.sample.model_dump())
    return BiometricAuthResponse(match=match, probability=prob, threshold=threshold, timestamp=datetime.utcnow())

