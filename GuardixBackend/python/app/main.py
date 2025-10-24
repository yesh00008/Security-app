from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.core.config import settings
from app.models.schemas import HealthResponse
from app.routes import auth as auth_routes
from app.routes import scan as scan_routes
from app.routes import biometric as biometric_routes
from app.routes import ids as ids_routes
from app.routes import federated as federated_routes
from app.routes import models as models_routes
from app.routes import storage as storage_routes
from app.routes import utilities as utilities_routes
from app.routes import performance as performance_routes
from app.routes import network as network_routes
from app.routes import security as security_routes
from app.routes import anomaly as anomaly_routes
from app.routes import realtime as realtime_routes


app = FastAPI(title=settings.app_name, description="Backend API for Guardix Mobile Security Application", version=settings.version)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # tighten in production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/", response_model=HealthResponse)
async def root():
    return HealthResponse(message="Guardix Security API is running", version=settings.version, status="healthy")


# Routers
app.include_router(auth_routes.router)
app.include_router(scan_routes.router)
app.include_router(biometric_routes.router)
app.include_router(ids_routes.router)
app.include_router(federated_routes.router)
app.include_router(models_routes.router)
app.include_router(storage_routes.router)
app.include_router(utilities_routes.router)
app.include_router(performance_routes.router)
app.include_router(network_routes.router)
app.include_router(security_routes.router)
app.include_router(anomaly_routes.router)
app.include_router(realtime_routes.router)


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="127.0.0.1", port=8000, reload=True)
