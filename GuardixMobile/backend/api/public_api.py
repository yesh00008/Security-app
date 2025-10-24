from fastapi import APIRouter, Header
from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any
from datetime import datetime
import random

router = APIRouter()


# ====== Schemas aligned with the Android app DTOs ======

class TokenRequestDto(BaseModel):
    user_id: str = Field(alias="user_id")


class TokenResponseDto(BaseModel):
    access_token: str = Field(alias="access_token")
    token_type: str = Field(alias="token_type")


class ApkFeatureDto(BaseModel):
    permissions: List[str] = []
    api_calls: List[str] = Field(default_factory=list, alias="api_calls")
    behaviors: List[str] = []
    metadata: Dict[str, str] = {}


class ApkScanRequestDto(BaseModel):
    package_name: Optional[str] = Field(default=None, alias="package_name")
    features: ApkFeatureDto


class ScanClassificationDto(BaseModel):
    label: str
    probability: float


class ApkScanResponseDto(BaseModel):
    scan_id: str = Field(alias="scan_id")
    classification: ScanClassificationDto
    timestamp: str


class PhishingScanRequestDto(BaseModel):
    url: Optional[str] = None
    text: Optional[str] = None


class PhishingScanResponseDto(BaseModel):
    scan_id: str = Field(alias="scan_id")
    probability: float
    is_phishing: bool = Field(alias="is_phishing")
    timestamp: str


class BiometricSampleDto(BaseModel):
    keystroke_timings: List[float]
    touch_pressure: List[float]
    touch_intervals: List[float]


class BiometricAuthRequestDto(BaseModel):
    user_id: str = Field(alias="user_id")
    sample: BiometricSampleDto


class BiometricAuthResponseDto(BaseModel):
    match: bool
    probability: float
    threshold: float
    timestamp: str


class TrafficRecordDto(BaseModel):
    bytes_in: int
    bytes_out: int
    connections: int
    failed_auth: int = Field(alias="failed_auth")


class IDSRequestDto(BaseModel):
    traffic: List[TrafficRecordDto]
    logs: List[Dict[str, str]] = []


class IDSAnomalyDto(BaseModel):
    index: int
    score: float
    record: Optional[TrafficRecordDto] = None


class IDSResponseDto(BaseModel):
    anomalies: List[IDSAnomalyDto]
    alert: bool
    score: float
    timestamp: str


class ModelInfoDto(BaseModel):
    name: str
    algorithm: str
    profile: str
    size_kb: Optional[float] = Field(default=None, alias="size_kb")


class ModelSummaryDto(BaseModel):
    active_profile: str = Field(alias="active_profile")
    models: List[ModelInfoDto]


class PerformanceOptimizeRequestDto(BaseModel):
    aggressive_mode: bool = False
    include_cache_clean: bool = True
    include_storage_clean: bool = True


class PerformanceOptimizeResponseDto(BaseModel):
    success: bool
    memory_freed: float
    storage_freed: float
    apps_optimized: int
    battery_life_improvement: int
    optimization_time: str
    recommendations: List[str]


class RunningAppDto(BaseModel):
    package: str
    name: str
    memory: float
    importance: str


class MemoryStatusResponseDto(BaseModel):
    total_ram: float
    available_ram: float
    used_ram: float
    usage_percent: float
    running_apps: List[RunningAppDto]
    optimization_tips: List[str]


class ThermalStatusResponseDto(BaseModel):
    temperature: float
    thermal_state: str
    cooling_recommendations: List[str]


class NetworkAppUsageDto(BaseModel):
    name: str
    package: str
    download: float
    upload: float


class NetworkInfoDto(BaseModel):
    type: str
    ssid: Optional[str] = None
    ip_address: Optional[str] = None
    signal_strength: Optional[str] = None


class NetworkUsageStatsDto(BaseModel):
    download_today: float
    upload_today: float
    download_speed: float
    upload_speed: float


class NetworkUsageResponseDto(BaseModel):
    current_network: NetworkInfoDto
    usage_stats: NetworkUsageStatsDto
    top_apps: List[NetworkAppUsageDto]
    connection_quality: str


class StorageOverviewResponseDto(BaseModel):
    total_storage: float
    used_storage: float
    available_storage: float
    usage_percentage: float


class AnomalyRequestDto(BaseModel):
    metrics: List[float]


class AnomalyResponseDto(BaseModel):
    label: str
    score: float
    probability: float


class SecurityOverviewResponseDto(BaseModel):
    security_score: int
    threat_summary: Dict[str, int]
    recent_events: List[Dict[str, Any]]
    recommendations: List[str]
    protection_modules: Dict[str, Any] = Field(alias="protection_modules")


def _ok_token_header(authorization: Optional[str]) -> bool:
    # Accept any Bearer token for demo; in real app validate JWT or session
    return authorization is None or authorization.startswith("Bearer ")


@router.post("/auth/login", response_model=TokenResponseDto)
async def login(request: TokenRequestDto):
    token = f"demo_{request.user_id}_{int(datetime.now().timestamp())}"
    return TokenResponseDto(access_token=token, token_type="bearer")


@router.post("/scan/apk", response_model=ApkScanResponseDto)
async def scan_apk(request: ApkScanRequestDto, authorization: Optional[str] = Header(default=None, alias="Authorization")):
    if not _ok_token_header(authorization):
        # Still return a result; keep demo permissive
        pass

    # Simple heuristic classification based on features
    score = 0.1
    risky_perms = {"READ_SMS", "RECEIVE_SMS", "SYSTEM_ALERT_WINDOW", "WRITE_SETTINGS", "READ_CONTACTS"}
    score += 0.2 * sum(1 for p in request.features.permissions if p in risky_perms)
    score += 0.1 * len([a for a in request.features.api_calls if a.lower() in ("exec", "sendtextmessage", "dexclassloader")])
    score = min(score, 0.99)
    label = "safe" if score < 0.4 else ("suspicious" if score < 0.7 else "malicious")
    resp = ApkScanResponseDto(
        scan_id=f"apk_{random.randint(1000, 999999)}",
        classification=ScanClassificationDto(label=label, probability=float(round(max(score, 1 - score), 3))),
        timestamp=datetime.now().isoformat()
    )
    return resp


@router.post("/scan/phishing", response_model=PhishingScanResponseDto)
async def scan_phishing(request: PhishingScanRequestDto, authorization: Optional[str] = Header(default=None, alias="Authorization")):
    txt = (request.url or "") + " " + (request.text or "")
    indicators = ["login", "password", "verify", "bank", "urgent", "suspend", "click here", "update"]
    hits = sum(1 for w in indicators if w in txt.lower())
    probability = min(0.1 + 0.15 * hits, 0.98)
    is_phishing = probability >= 0.6
    return PhishingScanResponseDto(
        scan_id=f"ph_{random.randint(1000, 999999)}",
        probability=float(round(probability, 3)),
        is_phishing=is_phishing,
        timestamp=datetime.now().isoformat()
    )


@router.post("/auth/biometric", response_model=BiometricAuthResponseDto)
async def biometric_auth(request: BiometricAuthRequestDto, authorization: Optional[str] = Header(default=None, alias="Authorization")):
    # Simple similarity score: lower variance assumed closer to profile
    def _score(arr: List[float]) -> float:
        if not arr:
            return 0.0
        avg = sum(arr) / len(arr)
        var = sum((x - avg) ** 2 for x in arr) / len(arr)
        return max(0.0, 1.0 - min(var / 10.0, 1.0))

    parts = [
        _score(request.sample.keystroke_timings),
        _score(request.sample.touch_pressure),
        _score(request.sample.touch_intervals),
    ]
    probability = float(round(sum(parts) / 3.0, 3))
    threshold = 0.6
    return BiometricAuthResponseDto(
        match=probability >= threshold,
        probability=probability,
        threshold=threshold,
        timestamp=datetime.now().isoformat(),
    )


@router.post("/monitor/ids", response_model=IDSResponseDto)
async def monitor_ids(request: IDSRequestDto, authorization: Optional[str] = Header(default=None, alias="Authorization")):
    anomalies: List[IDSAnomalyDto] = []
    total_score = 0.0
    for idx, rec in enumerate(request.traffic):
        score = 0.0
        if rec.failed_auth > 3:
            score += 0.4
        if rec.connections > 100:
            score += 0.3
        if rec.bytes_out > 10_000_000:
            score += 0.3
        total_score += score
        if score >= 0.5:
            anomalies.append(IDSAnomalyDto(index=idx, score=float(round(score, 3)), record=rec))
    alert = any(a.score >= 0.7 for a in anomalies)
    avg_score = float(round(total_score / max(1, len(request.traffic)), 3))
    return IDSResponseDto(anomalies=anomalies, alert=alert, score=avg_score, timestamp=datetime.now().isoformat())


@router.get("/models/", response_model=ModelSummaryDto)
async def models(authorization: Optional[str] = Header(default=None, alias="Authorization")):
    return ModelSummaryDto(
        active_profile="lite",
        models=[
            ModelInfoDto(name="malware", algorithm="NaiveBayes", profile="lite", size_kb=512.0),
            ModelInfoDto(name="phishing", algorithm="LogReg", profile="lite", size_kb=256.0),
            ModelInfoDto(name="anomaly", algorithm="IsolationForest", profile="lite", size_kb=384.0),
        ],
    )


@router.post("/performance/one-tap", response_model=PerformanceOptimizeResponseDto)
async def performance_optimize(request: PerformanceOptimizeRequestDto, authorization: Optional[str] = Header(default=None, alias="Authorization")):
    return PerformanceOptimizeResponseDto(
        success=True,
        memory_freed=round(random.uniform(100.0, 500.0), 2),
        storage_freed=round(random.uniform(200.0, 1500.0), 2),
        apps_optimized=random.randint(3, 12),
        battery_life_improvement=random.randint(5, 20),
        optimization_time="3s",
        recommendations=[
            "Close unused apps",
            "Reduce background sync",
            "Enable battery saver",
        ],
    )


@router.get("/performance/memory", response_model=MemoryStatusResponseDto)
async def performance_memory(authorization: Optional[str] = Header(default=None, alias="Authorization")):
    total = 4096.0
    used = round(random.uniform(1200.0, 3200.0), 1)
    avail = round(total - used, 1)
    percent = round((used / total) * 100.0, 1)
    return MemoryStatusResponseDto(
        total_ram=total,
        available_ram=avail,
        used_ram=used,
        usage_percent=percent,
        running_apps=[
            RunningAppDto(package="com.chat.app", name="Chat", memory=245.2, importance="foreground"),
            RunningAppDto(package="com.video.app", name="Video", memory=512.6, importance="background"),
        ],
        optimization_tips=["Close heavy apps", "Clear cache", "Disable animations"],
    )


@router.get("/performance/thermal", response_model=ThermalStatusResponseDto)
async def performance_thermal(authorization: Optional[str] = Header(default=None, alias="Authorization")):
    temps = [32.5, 35.1, 37.0, 39.3]
    t = random.choice(temps)
    state = "normal" if t < 36 else ("warm" if t < 38.5 else "hot")
    recs = ["Avoid direct sunlight", "Close intensive apps"]
    if state == "hot":
        recs.append("Enable cooling measures")
    return ThermalStatusResponseDto(temperature=t, thermal_state=state, cooling_recommendations=recs)


@router.get("/network-tools/usage", response_model=NetworkUsageResponseDto)
async def network_usage(authorization: Optional[str] = Header(default=None, alias="Authorization")):
    current = NetworkInfoDto(type="WIFI", ssid="GuardixNet", ip_address="192.168.1.100", signal_strength="-60 dBm")
    stats = NetworkUsageStatsDto(
        download_today=round(random.uniform(0.2, 3.5), 2),
        upload_today=round(random.uniform(0.05, 0.8), 2),
        download_speed=round(random.uniform(10.0, 120.0), 1),
        upload_speed=round(random.uniform(2.0, 40.0), 1),
    )
    apps = [
        NetworkAppUsageDto(name="YouTube", package="com.google.android.youtube", download=1.2, upload=0.02),
        NetworkAppUsageDto(name="Chrome", package="com.android.chrome", download=0.45, upload=0.05),
        NetworkAppUsageDto(name="Spotify", package="com.spotify.music", download=0.22, upload=0.01),
    ]
    quality = random.choice(["excellent", "good", "fair"])  # keep values aligned with UI expectations
    return NetworkUsageResponseDto(current_network=current, usage_stats=stats, top_apps=apps, connection_quality=quality)


@router.get("/storage/storage-overview", response_model=StorageOverviewResponseDto)
async def storage_overview(authorization: Optional[str] = Header(default=None, alias="Authorization")):
    total = 128_000.0
    used = round(random.uniform(20_000.0, 96_000.0), 1)
    avail = round(total - used, 1)
    percent = round((used / total) * 100.0, 1)
    return StorageOverviewResponseDto(total_storage=total, used_storage=used, available_storage=avail, usage_percentage=percent)


@router.post("/anomaly/behavior", response_model=AnomalyResponseDto)
async def anomaly_behavior(request: AnomalyRequestDto, authorization: Optional[str] = Header(default=None, alias="Authorization")):
    # Simple anomaly score (mock): higher variance => higher anomaly score
    xs = request.metrics
    if not xs:
        return AnomalyResponseDto(label="normal", score=0.05, probability=0.95)
    avg = sum(xs) / len(xs)
    var = sum((x - avg) ** 2 for x in xs) / len(xs)
    score = float(min(1.0, var / 10.0))
    label = "anomaly" if score > 0.6 else "normal"
    prob = float(round(0.5 + abs(score - 0.5), 3))
    return AnomalyResponseDto(label=label, score=float(round(score, 3)), probability=prob)


@router.post("/anomaly/system", response_model=AnomalyResponseDto)
async def anomaly_system(request: AnomalyRequestDto, authorization: Optional[str] = Header(default=None, alias="Authorization")):
    # Same mock logic for system
    return await anomaly_behavior(request, authorization)


@router.get("/security-tools/overview", response_model=SecurityOverviewResponseDto)
async def security_overview(authorization: Optional[str] = Header(default=None, alias="Authorization")):
    return SecurityOverviewResponseDto(
        security_score=random.randint(70, 95),
        threat_summary={"malware": random.randint(0, 3), "phishing": random.randint(0, 2), "network": random.randint(0, 4)},
        recent_events=[
            {"id": "evt1", "type": "scan_completed", "severity": "low", "source": "scanner", "timestamp": datetime.now().isoformat()},
        ],
        recommendations=["Enable 2FA", "Review app permissions", "Keep OS updated"],
        protection_modules={
            "firewall": {"status": "active"},
            "realtime_protection": {"status": "active"},
            "web_protection": {"status": "active"},
        },
    )


# Expose router as public_router for main.py
public_router = router

