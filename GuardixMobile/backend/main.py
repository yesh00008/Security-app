from fastapi import FastAPI, HTTPException, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Optional, Dict, Any
import logging
from datetime import datetime
import uuid

# Routers that expose endpoints consumed by the mobile app
from api.public_api import public_router
from api.realtime import realtime_router

# Internal services used by background scan endpoints
from services.threat_detector import ThreatDetector
from services.anomaly_detector import AnomalyDetector
from services.performance_monitor import PerformanceMonitor
from services.network_analyzer import NetworkAnalyzer

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="Guardix Mobile Backend",
    description="Advanced Security & Performance Monitoring API",
    version="1.0.0"
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Configure this properly for production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Initialize services
threat_detector = ThreatDetector()
anomaly_detector = AnomalyDetector()
performance_monitor = PerformanceMonitor()
network_analyzer = NetworkAnalyzer()

# Include API routers
app.include_router(public_router)  # exposes /auth, /scan, /anomaly, /performance, /network-tools, /storage, etc.
app.include_router(realtime_router)

# Data Models
class ScanRequest(BaseModel):
    scan_type: str
    target: Optional[str] = None
    options: Optional[Dict[str, Any]] = None

class ScanResult(BaseModel):
    scan_id: str
    scan_type: str
    status: str
    start_time: datetime
    end_time: Optional[datetime] = None
    results: Dict[str, Any]
    threats_found: int
    confidence_score: float

# Global scan storage (in production, use a proper database)
active_scans: Dict[str, ScanResult] = {}

@app.get("/")
async def root():
    """Health check endpoint aligned with mobile client schema"""
    return {
        "message": "Guardix backend is running",
        "version": "1.0.0",
        "status": "ok"
    }

@app.post("/api/scan/start", response_model=Dict[str, str])
async def start_scan(request: ScanRequest, background_tasks: BackgroundTasks):
    """Start a security scan"""
    scan_id = str(uuid.uuid4())
    
    # Create scan result entry
    scan_result = ScanResult(
        scan_id=scan_id,
        scan_type=request.scan_type,
        status="running",
        start_time=datetime.now(),
        results={},
        threats_found=0,
        confidence_score=0.0
    )
    
    active_scans[scan_id] = scan_result
    
    # Start background scan
    background_tasks.add_task(perform_scan, scan_id, request)
    
    return {"scan_id": scan_id, "status": "started"}

@app.get("/api/scan/{scan_id}", response_model=ScanResult)
async def get_scan_status(scan_id: str):
    """Get scan status and results"""
    if scan_id not in active_scans:
        raise HTTPException(status_code=404, detail="Scan not found")
    
    return active_scans[scan_id]

@app.get("/api/scan/{scan_id}/results")
async def get_scan_results(scan_id: str):
    """Get detailed scan results"""
    if scan_id not in active_scans:
        raise HTTPException(status_code=404, detail="Scan not found")
    
    scan = active_scans[scan_id]
    if scan.status != "completed":
        raise HTTPException(status_code=400, detail="Scan not completed yet")
    
    return scan.results

async def perform_scan(scan_id: str, request: ScanRequest):
    """Background task to perform the actual scan"""
    scan = active_scans[scan_id]
    
    try:
        results = {}
        threats_found = 0
        confidence_score = 0.0
        
        if request.scan_type == "quick":
            # Quick scan - basic threat detection
            threats = await threat_detector.quick_scan()
            results["threats"] = threats
            threats_found = len(threats)
            confidence_score = 0.85
            
        elif request.scan_type == "full":
            # Full system scan
            threats = await threat_detector.full_scan()
            anomalies = await anomaly_detector.detect_anomalies()
            perf_issues = await performance_monitor.check_performance()
            
            results["threats"] = threats
            results["anomalies"] = anomalies
            results["performance"] = perf_issues
            
            threats_found = len(threats) + len(anomalies)
            confidence_score = 0.95
            
        elif request.scan_type == "network":
            # Network security scan  
            network_threats = await network_analyzer.scan_network()
            results["network_threats"] = network_threats
            threats_found = len(network_threats)
            confidence_score = 0.90
            
        elif request.scan_type == "malware":
            # Malware-specific scan
            malware = await threat_detector.malware_scan()
            results["malware"] = malware
            threats_found = len(malware)
            confidence_score = 0.92
            
        elif request.scan_type == "privacy":
            # Privacy audit
            privacy_issues = await threat_detector.privacy_audit()
            results["privacy_issues"] = privacy_issues
            threats_found = len(privacy_issues)
            confidence_score = 0.88
        
        # Update scan results
        scan.status = "completed"
        scan.end_time = datetime.now()
        scan.results = results
        scan.threats_found = threats_found
        scan.confidence_score = confidence_score
        
        logger.info(f"Scan {scan_id} completed with {threats_found} threats found")
        
    except Exception as e:
        logger.error(f"Scan {scan_id} failed: {str(e)}")
        scan.status = "failed"
        scan.end_time = datetime.now()
        scan.results = {"error": str(e)}

@app.get("/api/dashboard/stats")
async def get_dashboard_stats():
    """Get dashboard statistics"""
    # Simulate real-time data
    return {
        "security_score": 85,
        "threats_blocked": 1247,
        "apps_scanned": 156,
        "last_scan": "Never",
        "performance": {
            "cpu_usage": 45.2,
            "memory_usage": 67.8,
            "storage_free": 32.1,
            "battery_level": 84
        },
        "network": {
            "status": "secure",
            "download_speed": 85.4,
            "upload_speed": 23.1,
            "ping": 12
        }
    }

@app.get("/api/reports/summary")
async def get_reports_summary():
    """Get comprehensive security reports"""
    return {
        "security_overview": {
            "total_scans": 24,
            "threats_detected": 12,
            "false_positives": 2,
            "quarantined_files": 8
        },
        "performance_metrics": {
            "avg_cpu_usage": 35.2,
            "avg_memory_usage": 58.4,
            "peak_memory": 89.1,
            "apps_optimized": 15
        },
        "network_analysis": {
            "suspicious_connections": 3,
            "blocked_ips": 127,
            "data_usage": "2.4 GB",
            "avg_speed": 67.8
        },
        "ml_insights": {
            "anomalies_detected": 8,
            "behavior_patterns": 156,
            "prediction_accuracy": 94.2,
            "model_confidence": 87.5
        }
    }

if __name__ == "__main__":
    import os
    import uvicorn
    port = int(os.environ.get("PORT", "8000"))
    uvicorn.run(app, host="0.0.0.0", port=port)
