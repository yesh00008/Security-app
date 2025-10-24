# Guardix Backend Startup Script (PowerShell)
# Run this script to start the Python FastAPI backend

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Guardix Backend Startup Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Navigate to Python backend directory
$pythonBackend = Join-Path $PSScriptRoot "GuardixBackend\python"

if (-Not (Test-Path $pythonBackend)) {
    Write-Host "Error: Python backend directory not found!" -ForegroundColor Red
    Write-Host "Expected location: $pythonBackend" -ForegroundColor Yellow
    exit 1
}

Set-Location $pythonBackend
Write-Host "📂 Working directory: $pythonBackend" -ForegroundColor Green
Write-Host ""

# Check if Python is installed
try {
    $pythonVersion = python --version 2>&1
    Write-Host "✅ Python found: $pythonVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Python not found! Please install Python 3.8+" -ForegroundColor Red
    Write-Host "Download from: https://www.python.org/downloads/" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Check if virtual environment exists
$venvPath = Join-Path $pythonBackend "venv"
if (-Not (Test-Path $venvPath)) {
    Write-Host "📦 Creating virtual environment..." -ForegroundColor Yellow
    python -m venv venv
    Write-Host "✅ Virtual environment created" -ForegroundColor Green
}

# Activate virtual environment
Write-Host "🔄 Activating virtual environment..." -ForegroundColor Yellow
& "$venvPath\Scripts\Activate.ps1"
Write-Host "✅ Virtual environment activated" -ForegroundColor Green
Write-Host ""

# Install dependencies
Write-Host "📥 Installing dependencies..." -ForegroundColor Yellow
pip install -r requirements.txt --quiet
Write-Host "✅ Dependencies installed" -ForegroundColor Green
Write-Host ""

# Get local IP address
$localIP = (Get-NetIPAddress -AddressFamily IPv4 | Where-Object {$_.InterfaceAlias -notlike "*Loopback*" -and $_.InterfaceAlias -notlike "*VirtualBox*"} | Select-Object -First 1).IPAddress

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  🚀 Starting Guardix Backend API" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Backend URLs:" -ForegroundColor White
Write-Host "  • Local:           http://localhost:8000" -ForegroundColor Green
Write-Host "  • Network:         http://${localIP}:8000" -ForegroundColor Green
Write-Host "  • Android Emulator: http://10.0.2.2:8000" -ForegroundColor Green
Write-Host ""
Write-Host "API Documentation:" -ForegroundColor White
Write-Host "  • Swagger UI:      http://localhost:8000/docs" -ForegroundColor Cyan
Write-Host "  • ReDoc:           http://localhost:8000/redoc" -ForegroundColor Cyan
Write-Host ""
Write-Host "To connect from physical Android device:" -ForegroundColor Yellow
Write-Host "  Update API_BASE_URL in app/build.gradle.kts to:" -ForegroundColor Yellow
Write-Host "  buildConfigField(`"String`", `"API_BASE_URL`", `"`"http://${localIP}:8000/`"`")" -ForegroundColor White
Write-Host ""
Write-Host "Press Ctrl+C to stop the server" -ForegroundColor Red
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Start the server
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
