from datetime import datetime
from fastapi import APIRouter, Depends, HTTPException

from app.core.security import get_current_user
from app.models.schemas import (
    APKScanRequest,
    APKScanResponse,
    PhishingScanRequest,
    PhishingScanResponse,
    ScanClassification,
)
from app.services.malware import malware_service
from app.services.phishing import phishing_service


router = APIRouter(prefix="/scan", tags=["scan"])


@router.post("/apk", response_model=APKScanResponse)
async def scan_apk(req: APKScanRequest, user=Depends(get_current_user)):
    label, prob = malware_service.predict(req.features.model_dump())
    return APKScanResponse(
        scan_id=malware_service.new_scan_id(),
        classification=ScanClassification(label=label, probability=prob),
        timestamp=datetime.utcnow(),
    )


@router.post("/phishing", response_model=PhishingScanResponse)
async def scan_phishing(req: PhishingScanRequest, user=Depends(get_current_user)):
    if not (req.url or req.text):
        raise HTTPException(status_code=400, detail="Provide url or text")
    prob = phishing_service.score(req.url, req.text)
    return PhishingScanResponse(
        scan_id=phishing_service.new_scan_id(),
        probability=prob,
        is_phishing=prob >= 0.5,
        timestamp=datetime.utcnow(),
    )

