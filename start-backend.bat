@echo off
REM Guardix Backend Startup Script (Windows Batch)
REM Run this script to start the Python FastAPI backend

echo ========================================
echo   Guardix Backend Startup Script
echo ========================================
echo.

cd GuardixBackend\python

REM Check if Python is installed
python --version >nul 2>&1
if errorlevel 1 (
    echo Error: Python not found!
    echo Please install Python 3.8+ from https://www.python.org/downloads/
    pause
    exit /b 1
)

echo Python found!
echo.

REM Check if virtual environment exists
if not exist "venv" (
    echo Creating virtual environment...
    python -m venv venv
    echo Virtual environment created!
)

REM Activate virtual environment
echo Activating virtual environment...
call venv\Scripts\activate.bat
echo.

REM Install dependencies
echo Installing dependencies...
pip install -r requirements.txt
echo.

echo ========================================
echo   Starting Guardix Backend API
echo ========================================
echo.
echo Backend URLs:
echo   Local:            http://localhost:8000
echo   Android Emulator: http://10.0.2.2:8000
echo.
echo API Documentation:
echo   Swagger UI:       http://localhost:8000/docs
echo   ReDoc:            http://localhost:8000/redoc
echo.
echo Press Ctrl+C to stop the server
echo ========================================
echo.

REM Start the server
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
