from fastapi import APIRouter, WebSocket, WebSocketDisconnect, Depends, Query
import uuid
import asyncio
import logging
from typing import Optional

from app.core.security import get_current_user_ws
from app.websockets import manager
from app.services.realtime_data import realtime_service

router = APIRouter(prefix="/realtime", tags=["Real-time Data"])
logger = logging.getLogger(__name__)

@router.websocket("/ws")
async def websocket_endpoint(
    websocket: WebSocket, 
    token: Optional[str] = Query(None),
    data_types: Optional[str] = Query("all")
):
    connection_id = str(uuid.uuid4())
    user_id = None
    
    # Authenticate if token is provided
    if token:
        try:
            user = await get_current_user_ws(token)
            user_id = user.get("sub")
        except Exception as e:
            await websocket.close(code=1008, reason="Authentication failed")
            return
    
    # Accept connection
    await manager.connect(websocket, connection_id, user_id)
    
    # Parse requested data types
    requested_types = data_types.split(",") if data_types != "all" else ["all"]
    
    # Start data streaming task
    streaming_task = None
    
    try:
        # Define callback for sending data
        async def send_data(data):
            data_type = data.get("type", "unknown")
            if "all" in requested_types or data_type in requested_types:
                await manager.send_personal_message(data, connection_id)
        
        # Start streaming task
        streaming_task = asyncio.create_task(
            realtime_service.start_data_stream(send_data, interval=1.0)
        )
        
        # Keep connection alive
        while True:
            # Wait for client messages (can be used for control commands)
            data = await websocket.receive_text()
            logger.debug(f"Received message from client {connection_id}: {data}")
            
    except WebSocketDisconnect:
        logger.info(f"Client {connection_id} disconnected")
    except Exception as e:
        logger.error(f"Error in WebSocket connection {connection_id}: {str(e)}")
    finally:
        # Clean up
        if streaming_task:
            streaming_task.cancel()
            try:
                await streaming_task
            except asyncio.CancelledError:
                pass
        
        manager.disconnect(connection_id, user_id)

@router.get("/status")
async def get_connection_status(current_user: dict = Depends(get_current_user_ws)):
    """Get status of WebSocket connections"""
    user_id = current_user.get("sub")
    return {
        "total_connections": manager.get_connection_count(),
        "user_connections": manager.get_user_connection_count(user_id),
        "streaming_active": realtime_service.running
    }