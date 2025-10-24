# Guardix Backend API

FastAPI backend for an AI-powered Android security app.

Features:
- Malware detection `/scan/apk` (profile-aware MultinomialNB/RandomForest on APK features)
- Phishing detection `/scan/phishing` (TF-IDF + SGD/Logistic Regression)
- Behavioral biometric auth `/auth/biometric` (keystroke/touch similarity)
- Intrusion detection `/monitor/ids` (IsolationForest anomaly detection)
- Model registry `/models/` (inspect active lightweight models)
- Storage automation `/storage/*` (cleanup, duplicates, cloud backup)
- Performance utilities `/performance/*` (one tap optimize, memory & thermal snapshots)
- Network tools `/network-tools/*` (security scan, usage analytics)
- Security overview `/security-tools/overview` (dashboard summary)
- Anomaly detection `/anomaly/*` (behavior and system anomaly scoring)
- JWT authentication (`/auth/login`)

## Setup

1) Create virtual environment
```bash
python -m venv venv
venv\Scripts\activate  # Windows
# or: source venv/bin/activate
```

2) Install dependencies
```bash
pip install -r requirements.txt
```

3) Configure environment (optional)
```bash
set JWT_SECRET=change_me
set ENV=dev
set MODEL_PROFILE=lite  # or "standard"
# Mongo (optional)
set MONGO_ENABLED=false
set MONGO_URI=mongodb://localhost:27017
set MONGO_DB=guardix
```

4) Run the server
```bash
uvicorn app.main:app --reload --host 127.0.0.1 --port 8000
```

Docs: visit `http://127.0.0.1:8000/docs`

## Architecture

```
app/
  core/        # config, JWT security
  routes/      # FastAPI routers (auth, scan, biometric, ids, models, federated)
  services/    # ML services: malware, phishing, biometric, ids, federated, registry
  models/      # Pydantic schemas
  db/          # DB adapters (memory; Mongo optional)
models_store/  # Saved models (auto-created)
data/          # Sample data (placeholder)
```

Models auto-train small placeholders on first run if not found in `models_store/`.

## Security

- JWT required for all endpoints except `GET /` and `/auth/login`.
- Use HTTPS in production via a reverse proxy (e.g., Nginx + certs).

## Example Requests

1) Get token
```bash
curl -X POST http://127.0.0.1:8000/auth/login \
  -H "Content-Type: application/json" \
  -d '{"user_id": "demo"}'
```

2) Malware scan
```bash
TOKEN=... # from login
curl -X POST http://127.0.0.1:8000/scan/apk \
  -H "Authorization: Bearer %TOKEN%" \
  -H "Content-Type: application/json" \
  -d '{
        "package_name": "com.example.app",
        "features": {
          "permissions": ["INTERNET", "READ_SMS"],
          "api_calls": ["okhttp"],
          "behaviors": ["net"]
        }
      }'
```

3) Phishing scan
```bash
curl -X POST http://127.0.0.1:8000/scan/phishing \
  -H "Authorization: Bearer %TOKEN%" \
  -H "Content-Type: application/json" \
  -d '{"url": "http://phish.me/login"}'
```

4) Biometric auth
```bash
curl -X POST http://127.0.0.1:8000/auth/biometric \
  -H "Authorization: Bearer %TOKEN%" \
  -H "Content-Type: application/json" \
  -d '{
        "user_id": "demo",
        "sample": {
          "keystroke_timings": [120,130,110,125],
          "touch_pressure": [0.4,0.41,0.39],
          "touch_intervals": [80,75,82]
        }
      }'
```

5) IDS monitoring
```bash
curl -X POST http://127.0.0.1:8000/monitor/ids \
  -H "Authorization: Bearer %TOKEN%" \
  -H "Content-Type: application/json" \
  -d '{
        "traffic": [
          {"bytes_in": 1500, "bytes_out": 1400, "connections": 6, "failed_auth": 0},
          {"bytes_in": 6000, "bytes_out": 5000, "connections": 25, "failed_auth": 4}
        ]
      }'
```

6) Inspect active models
```bash
curl -X GET http://127.0.0.1:8000/models/ \
  -H "Authorization: Bearer %TOKEN%"
```

## Android Integration Guide (REST)

- Base URL: `https://<server>/`
- Auth flow:
  - Call `/auth/login` with device/user id to get JWT.
  - Include header `Authorization: Bearer <token>` in subsequent requests.
- Endpoints:
- `/scan/apk`: send extracted features (permissions, api_calls, behaviors).
- `/scan/phishing`: send `url` or `text`.
- `/auth/biometric`: send behavioral sample; backend compares to stored profile.
- `/monitor/ids`: periodically send traffic/log summaries from device.
 - `/models/`: read-only metadata to confirm which lightweight models are active.

Notes:
- On first biometric request per user, backend auto-enrolls a profile with the sample (demo behavior). In production, add explicit enrollment and thresholds per user.
- Replace in-memory storage with Mongo by setting `MONGO_ENABLED=true` and running MongoDB.

## Postman

Import `Guardix.postman_collection.json` in the project root to test endpoints.
