from fastapi import APIRouter, Depends
import asyncio
import random
from datetime import datetime

from app.core.security import get_current_user
from app.models.schemas import (
    PerformanceOptimizationRequest,
    PerformanceOptimizationResponse,
    MemoryStatusResponse,
    ThermalStatusResponse,
)


router = APIRouter(prefix="/performance", tags=["Performance & Optimization"])


@router.post("/one-tap", response_model=PerformanceOptimizationResponse)
async def one_tap_optimization(
    request: PerformanceOptimizationRequest,
    current_user: dict = Depends(get_current_user),
):
    """Simulate a one-tap performance optimization."""
    await asyncio.sleep(2)

    memory_freed = random.uniform(350, 850) * 1024 * 1024
    storage_freed = random.uniform(200, 600) * 1024 * 1024 if request.include_storage_clean else 0.0
    apps_optimized = random.randint(5, 18)
    battery_gain = random.randint(8, 22)

    recommendations = [
        "Enable scheduled optimizations",
        "Limit auto-start applications",
        "Reduce background sync interval",
        "Review high power consumption apps",
    ]
    if request.aggressive_mode:
        recommendations.append("Consider disabling animations for additional savings")

    return PerformanceOptimizationResponse(
        success=True,
        memory_freed=memory_freed,
        storage_freed=storage_freed,
        apps_optimized=apps_optimized,
        battery_life_improvement=battery_gain,
        optimization_time=datetime.utcnow().isoformat(),
        recommendations=recommendations,
    )


@router.get("/memory", response_model=MemoryStatusResponse)
async def memory_status(current_user: dict = Depends(get_current_user)):
    """Return current memory usage snapshot."""
    total_ram = 6 * 1024 * 1024 * 1024  # 6GB
    used_ram = random.uniform(2.5, 4.8) * 1024 * 1024 * 1024
    available_ram = total_ram - used_ram

    running_apps = [
        {
            "package": "com.social.app",
            "name": "Social Feed",
            "memory": random.uniform(180, 320) * 1024 * 1024,
            "importance": "foreground",
        },
        {
            "package": "com.mail.app",
            "name": "Mail",
            "memory": random.uniform(90, 150) * 1024 * 1024,
            "importance": "background",
        },
        {
            "package": "com.music.app",
            "name": "Music Player",
            "memory": random.uniform(80, 120) * 1024 * 1024,
            "importance": "cached",
        },
    ]

    tips = [
        "Close unused foreground apps",
        "Disable auto-start for rarely used apps",
        "Enable smart memory cleaner",
    ]

    return MemoryStatusResponse(
        total_ram=total_ram,
        available_ram=available_ram,
        used_ram=used_ram,
        usage_percent=(used_ram / total_ram) * 100,
        running_apps=running_apps,
        optimization_tips=tips,
    )


@router.get("/thermal", response_model=ThermalStatusResponse)
async def thermal_status(current_user: dict = Depends(get_current_user)):
    """Return device thermal readings."""
    temperature = random.uniform(32.5, 49.5)
    if temperature < 38:
        state = "NORMAL"
    elif temperature < 42:
        state = "LIGHT"
    elif temperature < 46:
        state = "MODERATE"
    else:
        state = "SEVERE"

    recommendations = [
        "Close intensive games",
        "Lower screen brightness",
        "Avoid charging while gaming",
    ]
    if state == "SEVERE":
        recommendations.append("Power off for a few minutes to cool")

    return ThermalStatusResponse(
        temperature=temperature,
        thermal_state=state,
        cooling_recommendations=recommendations,
    )

