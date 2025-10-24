from fastapi import WebSocket
from typing import Dict, List, Any
import json
import asyncio
import logging
from datetime import datetime

logger = logging.getLogger(__name__)

class ConnectionManager:
    """
    WebSocket connection manager to handle multiple client connections
    and broadcast real-time data to connected clients.
    """
    
    def __init__(self):
        self.active_connections: Dict[str, WebSocket] = {}
        self.user_connections: Dict[str, List[str]] = {}  # user_id -> list of connection_ids
        
    async def connect(self, websocket: WebSocket, connection_id: str, user_id: str = None):
        """Accept a new WebSocket connection"""
        await websocket.accept()
        self.active_connections[connection_id] = websocket
        
        if user_id:
            if user_id not in self.user_connections:
                self.user_connections[user_id] = []
            self.user_connections[user_id].append(connection_id)
            
        logger.info(f"New WebSocket connection: {connection_id}, User: {user_id}")
        
    def disconnect(self, connection_id: str, user_id: str = None):
        """Remove a WebSocket connection"""
        if connection_id in self.active_connections:
            del self.active_connections[connection_id]
            
        if user_id and user_id in self.user_connections:
            if connection_id in self.user_connections[user_id]:
                self.user_connections[user_id].remove(connection_id)
                
            if not self.user_connections[user_id]:
                del self.user_connections[user_id]
                
        logger.info(f"WebSocket disconnected: {connection_id}, User: {user_id}")
        
    async def send_personal_message(self, message: Any, connection_id: str):
        """Send a message to a specific connection"""
        if connection_id in self.active_connections:
            websocket = self.active_connections[connection_id]
            if isinstance(message, dict):
                message = {**message, "timestamp": datetime.utcnow().isoformat()}
                await websocket.send_json(message)
            else:
                await websocket.send_text(str(message))
                
    async def broadcast(self, message: Any):
        """Broadcast a message to all connected clients"""
        if isinstance(message, dict):
            message = {**message, "timestamp": datetime.utcnow().isoformat()}
            json_message = json.dumps(message)
            for connection in self.active_connections.values():
                await connection.send_text(json_message)
        else:
            for connection in self.active_connections.values():
                await connection.send_text(str(message))
                
    async def broadcast_to_user(self, message: Any, user_id: str):
        """Broadcast a message to all connections of a specific user"""
        if user_id in self.user_connections:
            if isinstance(message, dict):
                message = {**message, "timestamp": datetime.utcnow().isoformat()}
            
            for connection_id in self.user_connections[user_id]:
                await self.send_personal_message(message, connection_id)
                
    def get_connection_count(self) -> int:
        """Get the number of active connections"""
        return len(self.active_connections)
        
    def get_user_connection_count(self, user_id: str) -> int:
        """Get the number of active connections for a specific user"""
        if user_id in self.user_connections:
            return len(self.user_connections[user_id])
        return 0

# Global connection manager instance
manager = ConnectionManager()