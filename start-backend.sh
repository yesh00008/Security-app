#!/bin/bash
# Guardix Backend Startup Script (Linux/Mac)
# Run this script to start the Python FastAPI backend

echo "========================================"
echo "  Guardix Backend Startup Script"
echo "========================================"
echo ""

# Navigate to Python backend directory
cd "$(dirname "$0")/GuardixBackend/python" || exit 1

# Check if Python is installed
if ! command -v python3 &> /dev/null; then
    echo "❌ Python 3 not found! Please install Python 3.8+"
    exit 1
fi

echo "✅ Python found: $(python3 --version)"
echo ""

# Check if virtual environment exists
if [ ! -d "venv" ]; then
    echo "📦 Creating virtual environment..."
    python3 -m venv venv
    echo "✅ Virtual environment created"
fi

# Activate virtual environment
echo "🔄 Activating virtual environment..."
source venv/bin/activate
echo "✅ Virtual environment activated"
echo ""

# Install dependencies
echo "📥 Installing dependencies..."
pip install -r requirements.txt --quiet
echo "✅ Dependencies installed"
echo ""

# Get local IP address
LOCAL_IP=$(hostname -I | awk '{print $1}')

echo "========================================"
echo "  🚀 Starting Guardix Backend API"
echo "========================================"
echo ""
echo "Backend URLs:"
echo "  • Local:            http://localhost:8000"
echo "  • Network:          http://${LOCAL_IP}:8000"
echo "  • Android Emulator: http://10.0.2.2:8000"
echo ""
echo "API Documentation:"
echo "  • Swagger UI:       http://localhost:8000/docs"
echo "  • ReDoc:            http://localhost:8000/redoc"
echo ""
echo "To connect from physical Android device:"
echo "  Update API_BASE_URL in app/build.gradle.kts to:"
echo "  buildConfigField(\"String\", \"API_BASE_URL\", \"\\\"http://${LOCAL_IP}:8000/\\\"\")"
echo ""
echo "Press Ctrl+C to stop the server"
echo "========================================"
echo ""

# Start the server
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
