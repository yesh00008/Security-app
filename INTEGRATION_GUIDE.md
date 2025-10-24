# Guardix Application - Backend Integration Guide

## Overview
This guide explains how to integrate the Guardix Mobile frontend with backend services.

---

## Available Backend Options

### 1. Python FastAPI Backend (✅ RECOMMENDED - Currently Configured)
- **Location**: `GuardixBackend/python/`
- **Port**: 8000
- **Features**: REST API + WebSocket support
- **Status**: Fully implemented and integrated

### 2. Java Spring Boot Backend (Available)
- **Location**: `GuardixBackend/java/`
- **Port**: 8080 (default)
- **Features**: REST API + gRPC support
- **Status**: Framework ready, needs API implementation

### 3. Node.js Backend (Not Implemented)
- Can be added if needed

---

## Quick Start - Python Backend (Default)

### Prerequisites
- Python 3.8+ installed
- Android Studio for mobile app
- Android Emulator or Physical Device

### Step 1: Start Python Backend

```bash
# Navigate to Python backend directory
cd GuardixBackend/python

# Install dependencies
pip install -r requirements.txt

# Run the server
python -m app.main
```

Or use uvicorn directly:
```bash
cd GuardixBackend/python
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

**Backend will be available at**: `http://localhost:8000`

### Step 2: Configure Mobile App

The Android app is pre-configured to connect to:
- **Emulator**: `http://10.0.2.2:8000/` (maps to localhost:8000)
- **Physical Device**: Update `API_BASE_URL` in `app/build.gradle.kts`

### Step 3: Run Mobile App

1. Open `GuardixMobile` in Android Studio
2. Sync Gradle files
3. Run on emulator or device

---

## Network Configuration

### For Android Emulator
```kotlin
// Already configured in build.gradle.kts
buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8000/\"")
```

### For Physical Device
Find your computer's local IP address:

**Windows**:
```cmd
ipconfig
# Look for IPv4 Address (e.g., 192.168.1.100)
```

**Update build.gradle.kts**:
```kotlin
buildConfigField("String", "API_BASE_URL", "\"http://YOUR_IP:8000/\"")
// Example: "http://192.168.1.100:8000/"
```

### For Production
```kotlin
buildConfigField("String", "API_BASE_URL", "\"https://api.guardix.com/\"")
```

---

## Testing the Integration

### 1. Check Backend Health
```bash
curl http://localhost:8000/
```

Expected response:
```json
{
  "message": "Guardix Security API is running",
  "version": "1.0.0",
  "status": "healthy"
}
```

### 2. Test Authentication
```bash
curl -X POST http://localhost:8000/auth/login \
  -H "Content-Type: application/json" \
  -d '{"device_id":"test-device","device_name":"Test Phone"}'
```

### 3. Test from Android App
- Launch app
- Login with biometric or credentials
- Navigate to different sections (Security, Performance, Network)
- Check real-time data updates

---

## API Endpoints Available

### Authentication
- `POST /auth/login` - Device authentication
- `POST /auth/biometric` - Biometric verification

### Security Scanning
- `POST /scan/apk` - APK malware scanning
- `POST /scan/phishing` - URL phishing detection
- `GET /security-tools/overview` - Security overview

### Performance Monitoring
- `GET /performance/memory` - Memory status
- `GET /performance/thermal` - CPU temperature
- `POST /performance/one-tap` - System optimization

### Network Tools
- `GET /network-tools/usage` - Network usage stats
- Real-time WebSocket: `ws://localhost:8000/realtime/ws`

### Anomaly Detection
- `POST /anomaly/behavior` - Behavior analysis
- `POST /anomaly/system` - System anomaly detection

### Storage
- `GET /storage/storage-overview` - Storage statistics

---

## Using Java Spring Boot Backend (Alternative)

### Step 1: Implement REST Controllers

Create controllers matching the Python API endpoints:

```java
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody TokenRequest request) {
        // Implementation
    }
}
```

### Step 2: Update Mobile App Configuration

```kotlin
// In build.gradle.kts
buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8080/\"")
```

### Step 3: Run Spring Boot Backend

```bash
cd GuardixBackend/java
mvn spring-boot:run
```

---

## Environment Configuration

### Development (Current)
- Backend: `http://10.0.2.2:8000` (emulator) or `http://YOUR_IP:8000` (device)
- Debug mode enabled
- CORS: Allow all origins

### Staging
```kotlin
buildConfigField("String", "API_BASE_URL", "\"http://staging-api.guardix.com/\"")
```

### Production
```kotlin
buildConfigField("String", "API_BASE_URL", "\"https://api.guardix.com/\"")
```
- Enable SSL/TLS
- Implement proper authentication
- Restrict CORS origins
- Enable ProGuard obfuscation

---

## WebSocket Real-Time Updates

The app supports real-time data via WebSocket:

### Backend WebSocket Endpoint
```
ws://localhost:8000/realtime/ws?token=YOUR_TOKEN&data_types=all
```

### Data Types Available
- `system_metrics` - CPU, Memory, Disk usage
- `security_events` - Security alerts
- `process_list` - Running processes
- `network_status` - Network information

---

## Troubleshooting

### Backend Not Reachable
1. **Check backend is running**: Visit `http://localhost:8000` in browser
2. **Check firewall**: Allow connections on port 8000
3. **For physical device**: Ensure device and computer on same network
4. **Check IP address**: Verify correct local IP in build.gradle.kts

### Connection Refused
- **Emulator**: Use `10.0.2.2` instead of `localhost`
- **Physical Device**: Use computer's local IP (192.168.x.x)
- **Backend**: Ensure running with `--host 0.0.0.0`

### SSL/Certificate Errors (Production)
- Ensure valid SSL certificate
- Add certificate to Android app's network security config

### Authentication Failures
- Check token format
- Verify token expiration
- Ensure `Authorization: Bearer TOKEN` header format

---

## Performance Optimization

### Backend
- Enable caching for frequently accessed data
- Use connection pooling
- Implement rate limiting
- Enable compression

### Mobile App
- Implement offline caching
- Use efficient image loading
- Minimize API calls
- Batch requests when possible

---

## Security Best Practices

### Development
- ✅ Use test credentials
- ✅ Enable debug logging
- ✅ Allow all CORS origins

### Production
- ✅ Use HTTPS only
- ✅ Implement proper JWT authentication
- ✅ Restrict CORS to app domain
- ✅ Enable certificate pinning
- ✅ Obfuscate code with ProGuard
- ✅ Implement API rate limiting
- ✅ Store secrets in Android Keystore

---

## Monitoring & Logging

### Backend Logs
```bash
# View real-time logs
tail -f app.log
```

### Mobile App Logs
```bash
# View Android logs
adb logcat | grep Guardix
```

---

## Next Steps

1. ✅ Start Python backend
2. ✅ Configure network settings for your device
3. ✅ Run mobile app and test features
4. 🔄 Implement additional endpoints as needed
5. 🔄 Deploy to staging/production environment

---

## Support

For issues or questions:
- Check logs for error messages
- Verify network connectivity
- Ensure all dependencies installed
- Review API endpoint documentation
