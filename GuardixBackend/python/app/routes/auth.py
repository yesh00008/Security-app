from fastapi import APIRouter

from app.core.security import create_access_token
from app.models.schemas import TokenRequest, TokenResponse


router = APIRouter(prefix="/auth", tags=["auth"])


@router.post("/login", response_model=TokenResponse)
async def login(req: TokenRequest):
    token = create_access_token(subject=req.user_id)
    return TokenResponse(access_token=token)

