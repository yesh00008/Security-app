from fastapi import APIRouter, Depends
from pydantic import BaseModel
from typing import List, Optional

from app.core.security import get_current_user
from app.services.federated import fedavg


class FedUpdate(BaseModel):
    weights: List[float]
    samples: Optional[int] = 1


class FedAggregateRequest(BaseModel):
    updates: List[FedUpdate]


class FedAggregateResponse(BaseModel):
    weights: List[float]


router = APIRouter(prefix="/federated", tags=["federated"])


@router.post("/aggregate", response_model=FedAggregateResponse)
async def aggregate(req: FedAggregateRequest, user=Depends(get_current_user)):
    weight_updates = [u.weights for u in req.updates]
    weights = [u.samples or 1 for u in req.updates]
    agg = fedavg(weight_updates, weights)
    return FedAggregateResponse(weights=agg)

