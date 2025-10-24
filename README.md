# 🛡️ Guardix - Comprehensive Mobile Security Application

Complete mobile security solution with integrated backend services for Android devices.

## 📋 Table of Contents
- [Features](#features)
- [Architecture](#architecture)
- [Quick Start](#quick-start)
- [Backend Integration](#backend-integration)
- [Development](#development)
- [Deployment](#deployment)
- [API Documentation](#api-documentation)

---

## ✨ Features

### Mobile App (Android)
- 🔐 **Security Scanning**: APK malware detection, phishing URL analysis
- 📊 **Performance Monitoring**: Real-time CPU, memory, and storage tracking
- 🌐 **Network Tools**: Bandwidth monitoring, connection analysis
- 🔍 **Anomaly Detection**: AI-powered behavior and system anomaly detection
- 🔒 **Biometric Authentication**: Fingerprint/face recognition
- 🚀 **System Optimization**: One-tap performance boost
- 📱 **Real-time Updates**: WebSocket-based live data streaming

### Backend Services
- 🐍 **Python FastAPI**: REST API + WebSocket support
- ☕ **Java Spring Boot**: Enterprise-grade framework (optional)
- 🔄 **Real-time Communication**: WebSocket for live updates
- 🤖 **ML Models**: Integrated machine learning for threat detection
- 📈 **Comprehensive APIs**: 13+ endpoint categories

---

## 🏗️ Architecture

```
Guardix Application
├── GuardixMobile/              # Android Frontend
│   ├── app/
│   │   ├── src/main/java/com/guardix/mobile/
│   │   │   ├── data/           # Data layer (API, DB, Models)
│   │   │   ├── di/             # Dependency Injection
│   │   │   ├── ui/             # UI Components & Screens
│   │   │   └── ...
│   │   └── build.gradle.kts
│   └── ...
│
├── GuardixBackend/             # Backend Services
│   ├── python/                 # FastAPI Backend (Primary)
│   │   ├── app/
│   │   │   ├── routes/         # API Endpoints
│   │   │   ├── services/       # Business Logic
│   │   │   ├── models/         # Data Models
│   │   │   └── main.py
│   │   └── requirements.txt
│   │
│   └── java/                   # Spring Boot Backend (Optional)
│       ├── src/main/java/
│       └── pom.xml
│
├── start-backend.ps1           # Backend startup script (Windows)
├── start-backend.bat           # Backend startup script (Windows)
├── start-backend.sh            # Backend startup script (Linux/Mac)
└── INTEGRATION_GUIDE.md        # Detailed integration guide
```

---

## 🚀 Quick Start

### Prerequisites
- **Python 3.8+** (for backend)
- **Android Studio** (for mobile app)
- **Android Emulator** or Physical Android Device
- **Git** (optional, for cloning)

### 1. Start the Backend

**Option A: Using PowerShell (Windows - Recommended)**
```powershell
.\start-backend.ps1
```

**Option B: Using Command Prompt (Windows)**
```cmd
start-backend.bat
```

**Option C: Using Bash (Linux/Mac)**
```bash
chmod +x start-backend.sh
./start-backend.sh
```

**Option D: Manual Setup**
```bash
cd GuardixBackend/python
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

Backend will be available at:
- Local: `http://localhost:8000`
- Emulator: `http://10.0.2.2:8000`
- Docs: `http://localhost:8000/docs`

### 2. Configure Mobile App

**For Android Emulator** (Default - No changes needed):
```kotlin
// Already configured in app/build.gradle.kts
buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8000/\"")
```

**For Physical Android Device**:
1. Find your computer's IP address:
   ```cmd
   # Windows
   ipconfig
   
   # Linux/Mac
   ifconfig
   ```
2. Update `GuardixMobile/app/build.gradle.kts`:
   ```kotlin
   buildConfigField("String", "API_BASE_URL", "\"http://YOUR_IP:8000/\"")
   // Example: "http://192.168.1.100:8000/"
   ```
3. Ensure phone and computer are on the same WiFi network

### 3. Run Mobile App

1. Open `GuardixMobile` folder in Android Studio
2. Wait for Gradle sync to complete
3. Select your emulator or connected device
4. Click **Run** ▶️

---

## 🔗 Backend Integration

The application supports multiple backend options:

### Python FastAPI (Default ✅)
- **Port**: 8000
- **Features**: REST API, WebSocket, ML models
- **Status**: Fully implemented and tested
- **Startup**: Use `start-backend.ps1` or manual commands above

### Java Spring Boot (Optional)
- **Port**: 8080
- **Features**: REST API, gRPC support
- **Status**: Framework ready, requires API implementation
- **Startup**: 
  ```bash
  cd GuardixBackend/java
  mvn spring-boot:run
  ```
- **Configuration**: Update `app/build.gradle.kts`:
  ```kotlin
  buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8080/\"")
  ```

### Node.js (Not Implemented)
- Can be added if needed
- Default port would be 3000

**Switch between backends** by updating `BackendConfig.kt`:
```kotlin
val ACTIVE_BACKEND = BackendType.PYTHON  // or JAVA, NODEJS
```

---

## 👨‍💻 Development

### Mobile App Development

**Project Structure:**
```
app/src/main/java/com/guardix/mobile/
├── data/
│   ├── remote/         # API clients, repositories
│   ├── realtime/       # WebSocket, real-time data
│   ├── security/       # Security services
│   └── performance/    # Performance monitoring
├── di/                 # Hilt dependency injection
├── ui/
│   ├── screens/        # Compose screens
│   ├── components/     # Reusable UI components
│   └── theme/          # App theming
└── MainActivity.kt
```

**Key Technologies:**
- Kotlin
- Jetpack Compose (UI)
- Hilt (Dependency Injection)
- Retrofit (HTTP Client)
- OkHttp (WebSocket)
- Coroutines & Flow
- Material Design 3

**Build Variants:**
- `debug` - Development build with logging
- `release` - Production build with ProGuard
- `staging` - Staging environment build

### Backend Development

**Python FastAPI Structure:**
```
GuardixBackend/python/app/
├── routes/             # API endpoints
│   ├── auth.py         # Authentication
│   ├── scan.py         # Malware/phishing scanning
│   ├── performance.py  # Performance APIs
│   ├── network.py      # Network tools
│   ├── security.py     # Security tools
│   ├── anomaly.py      # Anomaly detection
│   └── realtime.py     # WebSocket endpoints
├── services/           # Business logic
├── models/             # Pydantic models
├── core/               # Configuration
└── main.py             # App entry point
```

**Add New API Endpoint:**
1. Create route in `app/routes/`
2. Implement service logic in `app/services/`
3. Add Pydantic models in `app/models/schemas.py`
4. Register router in `main.py`
5. Update mobile app API service

---

## 📦 Deployment

### Backend Deployment

**Docker (Recommended):**
```dockerfile
FROM python:3.11-slim
WORKDIR /app
COPY GuardixBackend/python/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY GuardixBackend/python/app ./app
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
```

**Cloud Platforms:**
- AWS: EC2, Elastic Beanstalk, ECS
- Google Cloud: Cloud Run, App Engine
- Azure: App Service, Container Instances
- Heroku: Direct deployment

### Mobile App Deployment

**Build APK:**
```bash
cd GuardixMobile
./gradlew assembleRelease
```

APK location: `app/build/outputs/apk/release/app-release.apk`

**Build AAB (Google Play):**
```bash
./gradlew bundleRelease
```

AAB location: `app/build/outputs/bundle/release/app-release.aab`

**Update Production API URL:**
```kotlin
// In app/build.gradle.kts - release buildType
buildConfigField("String", "API_BASE_URL", "\"https://api.guardix.com/\"")
```

---

## 📚 API Documentation

### Available Endpoints

#### Authentication
- `POST /auth/login` - Device authentication
- `POST /auth/biometric` - Biometric verification

#### Security Scanning
- `POST /scan/apk` - APK malware analysis
- `POST /scan/phishing` - URL phishing detection
- `GET /security-tools/overview` - Security dashboard

#### Performance
- `GET /performance/memory` - Memory statistics
- `GET /performance/thermal` - CPU temperature
- `POST /performance/one-tap` - System optimization

#### Network Tools
- `GET /network-tools/usage` - Network usage stats
- WebSocket: `ws://localhost:8000/realtime/ws`

#### Anomaly Detection
- `POST /anomaly/behavior` - Behavior analysis
- `POST /anomaly/system` - System anomaly detection

#### Storage
- `GET /storage/storage-overview` - Storage statistics

#### Models
- `GET /models/` - ML model information

### Interactive API Docs
Once backend is running:
- **Swagger UI**: http://localhost:8000/docs
- **ReDoc**: http://localhost:8000/redoc

---

## 🔧 Configuration

### Environment Variables

**Backend (.env):**
```env
APP_NAME=Guardix Security API
VERSION=1.0.0
DEBUG=True
SECRET_KEY=your-secret-key-here
```

**Mobile App (build.gradle.kts):**
```kotlin
buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8000/\"")
buildConfigField("String", "DEFAULT_USER_ID", "\"guardix-device\"")
```

---

## 🧪 Testing

### Backend Tests
```bash
cd GuardixBackend/python
pytest
```

### Mobile App Tests
```bash
cd GuardixMobile
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest    # Instrumentation tests
```

---

## 📝 License

This project is proprietary. All rights reserved.

---

## 🤝 Support

For issues or questions:
- Check `INTEGRATION_GUIDE.md` for detailed integration steps
- Review API documentation at `/docs` endpoint
- Check application logs for error messages

---

## 🎯 Next Steps

1. ✅ Start the backend server
2. ✅ Configure network settings
3. ✅ Run the mobile app
4. ✅ Test the integration
5. 🔄 Deploy to staging/production

**Happy Coding! 🚀**
