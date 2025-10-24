from datetime import datetime
from fastapi import APIRouter, Depends

from app.core.security import get_current_user
from app.models.schemas import IDSRequest, IDSResponse
from app.services.ids import ids_service


router = APIRouter(prefix="/monitor", tags=["ids"])


@router.post("/ids", response_model=IDSResponse)
async def monitor_ids(req: IDSRequest, user=Depends(get_current_user)):
    alert, score, anomalies = ids_service.score([t.model_dump() for t in req.traffic])
    return IDSResponse(anomalies=anomalies, alert=alert, score=score, timestamp=datetime.utcnow())

