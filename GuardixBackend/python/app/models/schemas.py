from datetime import datetime
from typing import List, Optional, Dict, Any

from pydantic import BaseModel, Field, HttpUrl


class HealthResponse(BaseModel):
    message: str
    version: str
    status: str


class APKFeature(BaseModel):
    permissions: List[str] = []
    api_calls: List[str] = []
    metadata: dict = {}
    behaviors: List[str] = []


class APKScanRequest(BaseModel):
    package_name: Optional[str] = None
    features: APKFeature


class ScanClassification(BaseModel):
    label: str
    probability: float


class APKScanResponse(BaseModel):
    scan_id: str
    classification: ScanClassification
    timestamp: datetime = Field(default_factory=datetime.utcnow)


class PhishingScanRequest(BaseModel):
    url: Optional[HttpUrl] = None
    text: Optional[str] = None


class PhishingScanResponse(BaseModel):
    scan_id: str
    probability: float
    is_phishing: bool
    timestamp: datetime = Field(default_factory=datetime.utcnow)


class BiometricSample(BaseModel):
    # Simplified features; extend as needed
    keystroke_timings: List[float]
    touch_pressure: List[float]
    touch_intervals: List[float]


class BiometricAuthRequest(BaseModel):
    user_id: str
    sample: BiometricSample


class BiometricAuthResponse(BaseModel):
    match: bool
    probability: float
    threshold: float
    timestamp: datetime = Field(default_factory=datetime.utcnow)


class IDSLog(BaseModel):
    message: str
    level: str = "info"
    meta: Optional[dict] = None


class TrafficRecord(BaseModel):
    bytes_in: int
    bytes_out: int
    connections: int
    failed_auth: int = 0


class IDSRequest(BaseModel):
    logs: List[IDSLog] = []
    traffic: List[TrafficRecord] = []


class IDSResponse(BaseModel):
    anomalies: List[dict]
    alert: bool
    score: float
    timestamp: datetime = Field(default_factory=datetime.utcnow)


class TokenRequest(BaseModel):
    user_id: str


class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"


# Anomaly detection
class BehaviorAnomalyRequest(BaseModel):
    metrics: List[float]


class BehaviorAnomalyResponse(BaseModel):
    label: str
    score: float
    probability: float


class SystemAnomalyRequest(BaseModel):
    metrics: List[float]


class SystemAnomalyResponse(BaseModel):
    label: str
    score: float
    probability: float


# Storage & File Management
class FileCleanupRequest(BaseModel):
    target_paths: List[str] = []
    include_duplicates: bool = True
    min_size_mb: Optional[int] = None
    dry_run: bool = False


class FileCleanupResponse(BaseModel):
    success: bool
    total_space_freed: float
    files_removed: int
    cleanup_categories: Dict[str, Dict[str, Any]]
    large_files_found: List[Dict[str, Any]]
    duplicates_removed: int
    cleanup_recommendations: List[str]


class MediaOrganizationRequest(BaseModel):
    include_videos: bool = True
    include_faces: bool = True
    auto_albums: bool = True


class MediaOrganizationResponse(BaseModel):
    success: bool
    photos_organized: int
    videos_organized: int
    albums_created: List[Dict[str, Any]]
    duplicates_removed: int
    faces_detected: int
    face_groups_created: int
    organization_features: List[str]


class AppManagementRequest(BaseModel):
    include_system_apps: bool = False
    inactivity_days: int = 60


class AppManagementResponse(BaseModel):
    success: bool
    installed_apps: List[Dict[str, Any]]
    unused_apps: List[Dict[str, Any]]
    total_apps_size: float
    potential_space_savings: float
    apps_by_category: Dict[str, int]
    recommendations: List[str]


class DuplicateFinderRequest(BaseModel):
    scan_paths: List[str] = []
    similarity_threshold: Optional[int] = 90
    include_videos: bool = True


class DuplicateFinderResponse(BaseModel):
    success: bool
    duplicate_groups: List[Dict[str, Any]]
    total_duplicates_found: int
    total_space_wasted: float
    scan_locations: List[str]
    similarity_threshold: int
    recommendations: List[str]


class StorageAnalysisRequest(BaseModel):
    include_cache_breakdown: bool = True
    include_growth_trend: bool = True


class StorageAnalysisResponse(BaseModel):
    success: bool
    total_storage: float
    used_storage: float
    available_storage: float
    usage_percentage: float
    storage_breakdown: Dict[str, Dict[str, Any]]
    largest_files: List[Dict[str, Any]]
    growth_trend: str
    recommendations: List[str]


class CloudBackupRequest(BaseModel):
    categories: List[str] = []
    enable_auto_backup: bool = True
    backup_quality: str = "high"


class CloudBackupResponse(BaseModel):
    success: bool
    backup_categories: Dict[str, Dict[str, Any]]
    total_files_backed_up: int
    total_size_backed_up: float
    cloud_storage_info: Dict[str, Any]
    backup_schedule: str
    auto_backup_enabled: bool
    sync_status: str
    features: List[str]


# Utilities
class SystemHealthRequest(BaseModel):
    quick_scan: bool = False
    include_security: bool = True
    include_performance: bool = True


class SystemHealthResponse(BaseModel):
    health_score: int
    components: Dict[str, Dict[str, Any]]
    performance_metrics: Dict[str, Any]
    security_status: Dict[str, Any]
    issues_found: List[str]
    recommendations: List[str]
    optimization_actions: List[str]


class AppCloneRequest(BaseModel):
    package_name: str
    clone_count: int = 1
    enable_notifications: bool = True


class AppCloneResponse(BaseModel):
    success: bool
    clones_created: List[Dict[str, Any]]
    storage_used: float
    permissions_configured: List[str]
    coexistence_tips: List[str]


class SafeBoxRequest(BaseModel):
    files: List[str] = []
    action: str = "encrypt"
    password_hint: Optional[str] = None


class SafeBoxResponse(BaseModel):
    success: bool
    files_processed: List[Dict[str, Any]]
    vault_status: Dict[str, Any]
    security_advice: List[str]


class NotificationManagerRequest(BaseModel):
    silent_hours: Optional[List[str]] = None
    high_priority_apps: List[str] = []
    block_spam: bool = True


class NotificationManagerResponse(BaseModel):
    success: bool
    notifications_categorized: Dict[str, int]
    spam_detected: List[str]
    silent_mode_schedule: List[str]
    recommendations: List[str]


class ParentalControlsRequest(BaseModel):
    profile_name: str
    daily_screen_time_minutes: int = 120
    restricted_categories: List[str] = []


class ParentalControlsResponse(BaseModel):
    success: bool
    active_profile: Dict[str, Any]
    app_restrictions: List[Dict[str, Any]]
    usage_stats: Dict[str, Any]
    recommendations: List[str]


class DeviceInfoRequest(BaseModel):
    include_hardware: bool = True
    include_network: bool = True
    include_security: bool = True


class DeviceInfoResponse(BaseModel):
    device_overview: Dict[str, Any]
    hardware_info: Dict[str, Any]
    software_info: Dict[str, Any]
    sensor_info: Dict[str, Any]
    security_features: Dict[str, Any]


# Performance
class PerformanceOptimizationRequest(BaseModel):
    aggressive_mode: bool = False
    include_cache_clean: bool = True
    include_storage_clean: bool = True


class PerformanceOptimizationResponse(BaseModel):
    success: bool
    memory_freed: float
    storage_freed: float
    apps_optimized: int
    battery_life_improvement: int
    optimization_time: str
    recommendations: List[str]


class MemoryStatusResponse(BaseModel):
    total_ram: float
    available_ram: float
    used_ram: float
    usage_percent: float
    running_apps: List[Dict[str, Any]]
    optimization_tips: List[str]


class ThermalStatusResponse(BaseModel):
    temperature: float
    thermal_state: str
    cooling_recommendations: List[str]


# Network
class NetworkSecurityScanRequest(BaseModel):
    include_wifi: bool = True
    include_cellular: bool = True
    include_firewall: bool = True


class NetworkSecurityScanResponse(BaseModel):
    threats_detected: List[Dict[str, Any]]
    open_ports: List[int]
    wifi_security: Dict[str, Any]
    recommendations: List[str]


class NetworkUsageResponse(BaseModel):
    current_network: Dict[str, Any]
    usage_stats: Dict[str, Any]
    top_apps: List[Dict[str, Any]]
    connection_quality: str


# Security dashboard
class SecurityOverviewResponse(BaseModel):
    security_score: int
    threat_summary: Dict[str, Any]
    recent_events: List[Dict[str, Any]]
    recommendations: List[str]
    protection_modules: Dict[str, Any]
