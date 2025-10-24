---
description: Repository Information Overview
alwaysApply: true
---

# Repository Information Overview

## Repository Summary
Guardix is a comprehensive mobile security solution consisting of an Android mobile application (GuardixMobile) built with Jetpack Compose and a multi-model backend (GuardixBackend) that provides AI-powered security services through FastAPI, with support for alternative implementations in Node.js and Spring Boot.

## Repository Structure
- **GuardixMobile/**: Android application written in Kotlin using Jetpack Compose
- **GuardixBackend/**: Backend services providing AI-powered security features
  - **python/**: Primary FastAPI implementation
  - **node/**: Alternative Node.js microservices implementation
  - **java/**: Enterprise Spring Boot implementation

## Projects

### GuardixMobile (Android App)
**Configuration File**: app/build.gradle.kts

#### Language & Runtime
**Language**: Kotlin
**Version**: Java 11 compatibility
**Build System**: Gradle (Kotlin DSL)
**Package Manager**: Gradle

#### Dependencies
**Main Dependencies**:
- Compose BOM 2024.09.00
- Material 3 1.2.1
- Navigation Compose 2.8.0
- Lifecycle ViewModel Compose 2.8.5
- Retrofit 2.11.0 + Moshi 1.15.0
- Kotlinx Coroutines 1.8.1

**Development Dependencies**:
- JUnit 4.13.2
- Espresso 3.5.1
- Compose UI Testing

#### Frontend Code Structure
**Main Components**:
- **MainActivity.kt**: Entry point for the application
- **GuardixApp.kt**: Main app navigation container

**UI Layer**:
- **ui/theme/**: Design system (Color.kt, Theme.kt, Type.kt)
- **ui/components/**: Reusable UI components
  ```kotlin
  // CircularIndicator.kt - Example component
  @Composable
  fun SecurityScoreIndicator(
      score: Int,
      size: Dp = 200.dp,
      thickness: Dp = 12.dp,
      animationDuration: Int = 1000
  ) {
      // Implementation of circular security score indicator
  }
  ```

**Navigation**:
- **ui/navigation/MainNavigation.kt**: Navigation graph setup
  ```kotlin
  // Screen definitions
  sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
      object Home : Screen("home", "Home", Icons.Filled.Home)
      object Tools : Screen("tools", "Tools", Icons.Filled.Build)
      object Scan : Screen("scan", "Scan", Icons.Filled.Security)
      // Other screens...
  }
  
  @Composable
  fun MainNavigation() {
      val navController = rememberNavController()
      // Navigation implementation
  }
  ```

**Screens**:
- **ui/screens/HomeScreen.kt**: Main dashboard
- **ui/screens/security/SecurityToolsScreen.kt**: Security tools
- **ui/screens/performance/PerformanceToolsScreen.kt**: Performance tools
- **ui/screens/network/NetworkToolsScreen.kt**: Network tools
- **ui/screens/storage/StorageToolsScreen.kt**: Storage tools

**Network Layer**:
- **data/api/**: Retrofit API interfaces
- **data/repository/**: Repository implementations
  ```kotlin
  // Example API interface
  interface GuardixApi {
      @POST("auth/login")
      suspend fun login(@Body request: LoginRequest): LoginResponse
      
      @POST("scan/apk")
      suspend fun scanApp(@Body request: AppScanRequest): ScanResponse
      
      // Other API endpoints
  }
  ```

**View Models**:
- **viewmodel/**: ViewModel implementations for each screen
  ```kotlin
  class SecurityViewModel @Inject constructor(
      private val securityRepository: SecurityRepository
  ) : ViewModel() {
      // State management and business logic
  }
  ```

#### Build & Installation
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run tests
./gradlew test

# Install on connected device
./gradlew installDebug
```

#### Testing
**Framework**: JUnit, Espresso
**Test Location**: app/src/test/, app/src/androidTest/
**Run Command**:
```bash
./gradlew test
./gradlew connectedAndroidTest
```

### GuardixBackend (Python FastAPI)
**Configuration File**: requirements.txt

#### Language & Runtime
**Language**: Python
**Framework**: FastAPI 0.104.1
**Server**: Uvicorn 0.24.0
**Package Manager**: pip

#### Dependencies
**Main Dependencies**:
- FastAPI 0.104.1
- Pydantic 2.5.0
- PyJWT 2.9.0
- scikit-learn 1.5.2
- numpy 1.26.4
- motor 3.6.0 (MongoDB driver)
- python-dotenv (Environment variables)
- TensorFlow/PyTorch (ML model support)
- ONNX Runtime (Model optimization)

#### Backend Code Structure
**Main Application**:
- **app/main.py**: Entry point and FastAPI app configuration
  ```python
  from fastapi import FastAPI, Depends
  from fastapi.middleware.cors import CORSMiddleware
  from app.core.config import settings
  from app.routes import auth, scan, monitor, models
  
  app = FastAPI(
      title="Guardix Security API",
      description="AI-powered security services for mobile applications",
      version="1.0.0"
  )
  
  # Add CORS middleware
  app.add_middleware(
      CORSMiddleware,
      allow_origins=settings.CORS_ORIGINS,
      allow_credentials=True,
      allow_methods=["*"],
      allow_headers=["*"],
  )
  
  # Include routers
  app.include_router(auth.router, prefix="/auth", tags=["Authentication"])
  app.include_router(scan.router, prefix="/scan", tags=["Security Scanning"])
  app.include_router(monitor.router, prefix="/monitor", tags=["Monitoring"])
  app.include_router(models.router, prefix="/models", tags=["Model Management"])
  ```

**Core Configuration**:
- **app/core/config.py**: Environment variables and settings
  ```python
  from pydantic import BaseSettings
  
  class Settings(BaseSettings):
      API_V1_STR: str = "/api/v1"
      JWT_SECRET: str = "change_me_in_production"
      ALGORITHM: str = "HS256"
      ACCESS_TOKEN_EXPIRE_MINUTES: int = 60
      MODEL_PROFILE: str = "lite"  # "lite" or "standard"
      MONGO_ENABLED: bool = False
      MONGO_URI: str = "mongodb://localhost:27017"
      MONGO_DB: str = "guardix"
      CORS_ORIGINS: list = ["*"]
      
      class Config:
          env_file = ".env"
          
  settings = Settings()
  ```

**API Routes**:
- **app/routes/scan.py**: Malware and phishing scanning endpoints
  ```python
  from fastapi import APIRouter, Depends, HTTPException
  from app.core.security import get_current_user
  from app.models.scan import AppScanRequest, ScanResponse
  from app.services.malware import MalwareService
  
  router = APIRouter()
  malware_service = MalwareService()
  
  @router.post("/apk", response_model=ScanResponse)
  async def scan_app(
      request: AppScanRequest,
      current_user = Depends(get_current_user)
  ):
      """
      Analyze an Android app for malware based on its features
      """
      result = malware_service.analyze(
          package_name=request.package_name,
          features=request.features
      )
      return {
          "is_malicious": result.is_malicious,
          "confidence": result.confidence,
          "risk_level": result.risk_level,
          "details": result.details
      }
  ```

**ML Services**:
- **app/services/malware.py**: Malware detection service
  ```python
  import joblib
  import numpy as np
  from pathlib import Path
  from app.core.config import settings
  
  class MalwareService:
      def __init__(self):
          model_path = Path("models_store") / f"malware_{settings.MODEL_PROFILE}.joblib"
          self.model = self._load_model(model_path)
          self.vectorizer = self._load_vectorizer()
          
      def _load_model(self, path):
          if not path.exists():
              # Train a placeholder model if not exists
              self._train_placeholder_model(path)
          return joblib.load(path)
          
      def analyze(self, package_name, features):
          # Extract feature vector
          feature_vector = self._extract_features(features)
          
          # Make prediction
          prediction = self.model.predict([feature_vector])[0]
          confidence = np.max(self.model.predict_proba([feature_vector]))
          
          return MalwareResult(
              is_malicious=bool(prediction),
              confidence=float(confidence),
              risk_level=self._calculate_risk_level(confidence),
              details=self._generate_details(features, confidence)
          )
  ```

**Data Models**:
- **app/models/scan.py**: Pydantic models for request/response
  ```python
  from pydantic import BaseModel
  from typing import List, Dict, Optional
  
  class AppFeatures(BaseModel):
      permissions: List[str]
      api_calls: List[str]
      behaviors: List[str]
  
  class AppScanRequest(BaseModel):
      package_name: str
      features: AppFeatures
  
  class ScanResponse(BaseModel):
      is_malicious: bool
      confidence: float
      risk_level: str
      details: Dict[str, str]
  ```

**Authentication**:
- **app/core/security.py**: JWT authentication
  ```python
  from datetime import datetime, timedelta
  from typing import Optional
  from jose import JWTError, jwt
  from fastapi import Depends, HTTPException, status
  from fastapi.security import OAuth2PasswordBearer
  from app.core.config import settings
  
  oauth2_scheme = OAuth2PasswordBearer(tokenUrl="auth/login")
  
  def create_access_token(data: dict, expires_delta: Optional[timedelta] = None):
      to_encode = data.copy()
      expire = datetime.utcnow() + (expires_delta or timedelta(minutes=15))
      to_encode.update({"exp": expire})
      return jwt.encode(to_encode, settings.JWT_SECRET, algorithm=settings.ALGORITHM)
  
  async def get_current_user(token: str = Depends(oauth2_scheme)):
      credentials_exception = HTTPException(
          status_code=status.HTTP_401_UNAUTHORIZED,
          detail="Could not validate credentials",
          headers={"WWW-Authenticate": "Bearer"},
      )
      try:
          payload = jwt.decode(token, settings.JWT_SECRET, algorithms=[settings.ALGORITHM])
          user_id: str = payload.get("sub")
          if user_id is None:
              raise credentials_exception
      except JWTError:
          raise credentials_exception
      return user_id
  ```

#### Architecture
**Modular Structure**:
- **app/**: Main application package
  - **core/**: Configuration, JWT security, environment variables
  - **routes/**: FastAPI routers for all endpoints
  - **services/**: ML services and business logic
  - **models/**: Pydantic schemas and ML model definitions
  - **db/**: Database adapters (memory, MongoDB)
  - **utils/**: Utility functions, logging, error handling
- **models_store/**: Saved ML models
- **tests/**: Pytest test suite

#### ML Models & Features
**Malware Detection**:
- **Models**: MultinomialNB, RandomForest, CNN
- **Features**: App permissions, API calls, behavior patterns
- **Endpoint**: `/analyze/malware`, `/scan/apk`
- **Capabilities**: Real-time classification, confidence scoring, continuous learning

**Phishing Detection**:
- **Models**: TF-IDF + SGD/Logistic Regression, BERT-based NLP
- **Features**: URL analysis, text content analysis
- **Endpoint**: `/analyze/phishing`, `/scan/phishing`
- **Capabilities**: URL safety verification, text content analysis

**Biometric Authentication**:
- **Models**: SVM, KNN, Neural Networks
- **Features**: Keystroke dynamics, touch patterns, behavioral metrics
- **Endpoint**: `/auth/biometric`
- **Capabilities**: User verification, continuous authentication

**Intrusion Detection**:
- **Models**: IsolationForest, LSTM, Anomaly Detection
- **Features**: Network traffic patterns, system logs, behavior anomalies
- **Endpoint**: `/intrusion/detect`, `/monitor/ids`
- **Capabilities**: Real-time monitoring, anomaly detection, alert generation

**Federated Learning**:
- **Implementation**: Privacy-preserving model updates
- **Endpoint**: `/models/federated/update`
- **Capabilities**: Secure model aggregation, client-side training

#### Security & Performance
- JWT-based authentication for all endpoints
- Rate limiting middleware
- Request validation with Pydantic
- Low-latency prediction optimization
- Caching for frequent requests

#### Build & Installation
```bash
# Create virtual environment
python -m venv venv
venv\Scripts\activate  # Windows
# or: source venv/bin/activate

# Install dependencies
pip install -r requirements.txt
```

#### Usage & Operations
```bash
# Run the server
uvicorn app.main:app --reload --host 127.0.0.1 --port 8000
```

#### Testing
**Framework**: pytest
**Test Location**: tests/
**Run Command**:
```bash
pytest
```

### GuardixBackend (Node.js Microservices)
**Configuration File**: package.json

#### Language & Runtime
**Language**: JavaScript/TypeScript
**Framework**: Express.js
**Architecture**: Microservices
**Package Manager**: npm/yarn

#### Dependencies
**Main Dependencies**:
- Express.js (API gateway)
- TensorFlow.js (ML model integration)
- gRPC (Service communication)
- JWT (Authentication)
- Redis (Caching)
- Helmet.js (Security)

#### Backend Code Structure
**API Gateway**:
- **gateway-service/src/index.ts**: Main gateway entry point
  ```typescript
  import express from 'express';
  import helmet from 'helmet';
  import cors from 'cors';
  import rateLimit from 'express-rate-limit';
  import { createProxyMiddleware } from 'http-proxy-middleware';
  import { authMiddleware } from './middleware/auth';
  
  const app = express();
  const PORT = process.env.PORT || 3000;
  
  // Security middleware
  app.use(helmet());
  app.use(cors());
  app.use(express.json());
  
  // Rate limiting
  const limiter = rateLimit({
    windowMs: 15 * 60 * 1000, // 15 minutes
    max: 100 // limit each IP to 100 requests per windowMs
  });
  app.use(limiter);
  
  // Routes that don't require authentication
  app.use('/api/auth', createProxyMiddleware({ 
    target: 'http://auth-service:3001',
    changeOrigin: true
  }));
  
  // Routes that require authentication
  app.use('/api/malware', authMiddleware, createProxyMiddleware({ 
    target: 'http://malware-service:3002',
    changeOrigin: true
  }));
  
  app.use('/api/phishing', authMiddleware, createProxyMiddleware({ 
    target: 'http://phishing-service:3003',
    changeOrigin: true
  }));
  
  app.use('/api/biometric', authMiddleware, createProxyMiddleware({ 
    target: 'http://biometric-service:3004',
    changeOrigin: true
  }));
  
  // WebSocket proxy for intrusion detection
  app.use('/api/intrusion', authMiddleware, createProxyMiddleware({
    target: 'http://intrusion-service:3005',
    ws: true,
    changeOrigin: true
  }));
  
  app.listen(PORT, () => {
    console.log(`API Gateway running on port ${PORT}`);
  });
  ```

**Malware Service**:
- **malware-service/src/index.ts**: Malware detection service
  ```typescript
  import express from 'express';
  import * as tf from '@tensorflow/tfjs-node';
  import { loadModel, preprocessFeatures } from './model';
  
  const app = express();
  const PORT = process.env.PORT || 3002;
  let model: tf.LayersModel;
  
  app.use(express.json());
  
  // Initialize model
  (async () => {
    model = await loadModel();
    console.log('Malware detection model loaded');
  })();
  
  app.post('/api/malware', async (req, res) => {
    try {
      const { packageName, features } = req.body;
      
      // Preprocess features for the model
      const featureVector = preprocessFeatures(features);
      
      // Make prediction
      const tensorInput = tf.tensor2d([featureVector]);
      const prediction = model.predict(tensorInput) as tf.Tensor;
      const results = await prediction.array();
      
      // Get confidence score
      const confidence = results[0][0];
      const isMalicious = confidence > 0.5;
      
      // Clean up tensors
      tensorInput.dispose();
      prediction.dispose();
      
      res.json({
        is_malicious: isMalicious,
        confidence: confidence,
        risk_level: isMalicious ? 
          (confidence > 0.8 ? 'high' : 'medium') : 'low',
        details: {
          package_name: packageName,
          suspicious_permissions: features.permissions
            .filter(p => p.includes('SMS') || p.includes('LOCATION'))
        }
      });
    } catch (error) {
      console.error('Prediction error:', error);
      res.status(500).json({ error: 'Prediction failed' });
    }
  });
  
  app.listen(PORT, () => {
    console.log(`Malware service running on port ${PORT}`);
  });
  ```

**Authentication Service**:
- **auth-service/src/index.ts**: JWT authentication service
  ```typescript
  import express from 'express';
  import jwt from 'jsonwebtoken';
  import { v4 as uuidv4 } from 'uuid';
  
  const app = express();
  const PORT = process.env.PORT || 3001;
  const JWT_SECRET = process.env.JWT_SECRET || 'change_me_in_production';
  
  app.use(express.json());
  
  app.post('/api/auth/login', (req, res) => {
    const { user_id } = req.body;
    
    if (!user_id) {
      return res.status(400).json({ error: 'User ID is required' });
    }
    
    // In a real app, validate credentials here
    
    // Generate token
    const token = jwt.sign(
      { sub: user_id, jti: uuidv4() },
      JWT_SECRET,
      { expiresIn: '1h' }
    );
    
    res.json({ access_token: token, token_type: 'bearer' });
  });
  
  app.post('/api/auth/biometric', (req, res) => {
    // Forward to biometric service
    // This would be handled by the API gateway in production
  });
  
  app.listen(PORT, () => {
    console.log(`Auth service running on port ${PORT}`);
  });
  ```

**WebSocket Intrusion Service**:
- **intrusion-service/src/index.ts**: Real-time intrusion detection
  ```typescript
  import express from 'express';
  import http from 'http';
  import { Server } from 'socket.io';
  import { IsolationForest } from './models/isolation-forest';
  
  const app = express();
  const server = http.createServer(app);
  const io = new Server(server);
  const PORT = process.env.PORT || 3005;
  
  // Initialize anomaly detection model
  const anomalyDetector = new IsolationForest();
  
  io.on('connection', (socket) => {
    console.log('Client connected');
    
    socket.on('monitor', (data) => {
      // Process incoming telemetry
      const { traffic } = data;
      
      // Detect anomalies
      const anomalies = traffic.map(entry => {
        const features = [
          entry.bytes_in, 
          entry.bytes_out,
          entry.connections,
          entry.failed_auth
        ];
        
        const score = anomalyDetector.predict(features);
        return {
          timestamp: new Date(),
          is_anomaly: score < -0.5,
          score: score,
          details: entry
        };
      });
      
      // Send back results
      const hasAnomalies = anomalies.some(a => a.is_anomaly);
      if (hasAnomalies) {
        socket.emit('alert', {
          level: 'warning',
          message: 'Potential intrusion detected',
          anomalies: anomalies.filter(a => a.is_anomaly)
        });
      }
    });
    
    socket.on('disconnect', () => {
      console.log('Client disconnected');
    });
  });
  
  server.listen(PORT, () => {
    console.log(`Intrusion service running on port ${PORT}`);
  });
  ```

#### Architecture
**Microservices Structure**:
- **gateway-service/**: API routing, authentication, rate limiting
- **malware-service/**: Malware detection ML models
- **phishing-service/**: Phishing detection NLP models
- **biometric-service/**: Biometric authentication
- **intrusion-service/**: Real-time intrusion detection
- **model-registry/**: Model versioning and management

#### API Endpoints
- `/api/malware`: Malware detection
- `/api/phishing`: Phishing URL/text analysis
- `/api/auth/biometric`: Biometric authentication
- `/api/intrusion`: WebSocket stream for intrusion alerts
- `/api/metrics`: Prometheus metrics

#### Security & Performance
- JWT authentication
- Role-based access control
- Redis caching for ML inferences
- Helmet.js for HTTP security
- Docker containerization

### GuardixBackend (Spring Boot Enterprise)
**Configuration File**: pom.xml

#### Language & Runtime
**Language**: Java
**Framework**: Spring Boot
**ML Integration**: gRPC to Python services
**Package Manager**: Maven

#### Dependencies
**Main Dependencies**:
- Spring Boot
- Spring Security
- gRPC (Python ML service communication)
- Spring Actuator (Monitoring)
- SpringDoc OpenAPI (Documentation)

#### Backend Code Structure
**Main Application**:
- **src/main/java/com/guardix/backend/GuardixApplication.java**: Entry point
  ```java
  package com.guardix.backend;
  
  import org.springframework.boot.SpringApplication;
  import org.springframework.boot.autoconfigure.SpringBootApplication;
  import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
  
  @SpringBootApplication
  @EnableDiscoveryClient
  public class GuardixApplication {
      public static void main(String[] args) {
          SpringApplication.run(GuardixApplication.class, args);
      }
  }
  ```

**REST Controllers**:
- **src/main/java/com/guardix/backend/controller/MalwareController.java**: Malware analysis endpoint
  ```java
  package com.guardix.backend.controller;
  
  import com.guardix.backend.dto.AppScanRequest;
  import com.guardix.backend.dto.ScanResponse;
  import com.guardix.backend.service.MalwareService;
  import io.swagger.v3.oas.annotations.Operation;
  import io.swagger.v3.oas.annotations.tags.Tag;
  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.http.ResponseEntity;
  import org.springframework.security.access.prepost.PreAuthorize;
  import org.springframework.web.bind.annotation.PostMapping;
  import org.springframework.web.bind.annotation.RequestBody;
  import org.springframework.web.bind.annotation.RequestMapping;
  import org.springframework.web.bind.annotation.RestController;
  
  import javax.validation.Valid;
  
  @RestController
  @RequestMapping("/malware")
  @Tag(name = "Malware Detection", description = "APIs for malware analysis")
  public class MalwareController {
  
      private final MalwareService malwareService;
  
      @Autowired
      public MalwareController(MalwareService malwareService) {
          this.malwareService = malwareService;
      }
  
      @PostMapping("/analyze")
      @Operation(summary = "Analyze app for malware", 
                description = "Analyzes app behavior patterns for malicious activity")
      @PreAuthorize("hasRole('USER')")
      public ResponseEntity<ScanResponse> analyzeApp(@Valid @RequestBody AppScanRequest request) {
          ScanResponse response = malwareService.analyzeApp(request);
          return ResponseEntity.ok(response);
      }
  }
  ```

**gRPC Client**:
- **src/main/java/com/guardix/backend/grpc/MalwareGrpcClient.java**: Python ML service client
  ```java
  package com.guardix.backend.grpc;
  
  import com.guardix.grpc.MalwareRequest;
  import com.guardix.grpc.MalwareResponse;
  import com.guardix.grpc.MalwareServiceGrpc;
  import io.grpc.ManagedChannel;
  import io.grpc.ManagedChannelBuilder;
  import org.springframework.beans.factory.annotation.Value;
  import org.springframework.stereotype.Component;
  
  import javax.annotation.PostConstruct;
  import javax.annotation.PreDestroy;
  import java.util.concurrent.TimeUnit;
  
  @Component
  public class MalwareGrpcClient {
  
      @Value("${grpc.malware.host}")
      private String host;
  
      @Value("${grpc.malware.port}")
      private int port;
  
      private ManagedChannel channel;
      private MalwareServiceGrpc.MalwareServiceBlockingStub blockingStub;
  
      @PostConstruct
      public void init() {
          channel = ManagedChannelBuilder.forAddress(host, port)
                  .usePlaintext()
                  .build();
          blockingStub = MalwareServiceGrpc.newBlockingStub(channel);
      }
  
      public MalwareResponse analyzeMalware(String packageName, String[] permissions, 
                                           String[] apiCalls, String[] behaviors) {
          MalwareRequest request = MalwareRequest.newBuilder()
                  .setPackageName(packageName)
                  .addAllPermissions(List.of(permissions))
                  .addAllApiCalls(List.of(apiCalls))
                  .addAllBehaviors(List.of(behaviors))
                  .build();
  
          return blockingStub.analyze(request);
      }
  
      @PreDestroy
      public void shutdown() throws InterruptedException {
          channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
      }
  }
  ```

**Service Layer**:
- **src/main/java/com/guardix/backend/service/MalwareService.java**: Business logic
  ```java
  package com.guardix.backend.service;
  
  import com.guardix.backend.dto.AppScanRequest;
  import com.guardix.backend.dto.ScanResponse;
  import com.guardix.backend.grpc.MalwareGrpcClient;
  import com.guardix.grpc.MalwareResponse;
  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.cache.annotation.Cacheable;
  import org.springframework.stereotype.Service;
  
  @Service
  public class MalwareService {
  
      private final MalwareGrpcClient grpcClient;
  
      @Autowired
      public MalwareService(MalwareGrpcClient grpcClient) {
          this.grpcClient = grpcClient;
      }
  
      @Cacheable(value = "malwareResults", key = "#request.packageName")
      public ScanResponse analyzeApp(AppScanRequest request) {
          // Call Python ML service via gRPC
          MalwareResponse grpcResponse = grpcClient.analyzeMalware(
              request.getPackageName(),
              request.getFeatures().getPermissions().toArray(new String[0]),
              request.getFeatures().getApiCalls().toArray(new String[0]),
              request.getFeatures().getBehaviors().toArray(new String[0])
          );
  
          // Map gRPC response to REST response
          return ScanResponse.builder()
              .isMalicious(grpcResponse.getIsMalicious())
              .confidence(grpcResponse.getConfidence())
              .riskLevel(grpcResponse.getRiskLevel())
              .details(grpcResponse.getDetailsMap())
              .build();
      }
  }
  ```

**Security Configuration**:
- **src/main/java/com/guardix/backend/config/SecurityConfig.java**: JWT security
  ```java
  package com.guardix.backend.config;
  
  import com.guardix.backend.security.JwtAuthenticationFilter;
  import org.springframework.context.annotation.Bean;
  import org.springframework.context.annotation.Configuration;
  import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
  import org.springframework.security.config.annotation.web.builders.HttpSecurity;
  import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
  import org.springframework.security.config.http.SessionCreationPolicy;
  import org.springframework.security.web.SecurityFilterChain;
  import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
  
  @Configuration
  @EnableWebSecurity
  @EnableGlobalMethodSecurity(prePostEnabled = true)
  public class SecurityConfig {
  
      private final JwtAuthenticationFilter jwtAuthFilter;
  
      public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
          this.jwtAuthFilter = jwtAuthFilter;
      }
  
      @Bean
      public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
          http
              .csrf().disable()
              .authorizeRequests()
              .antMatchers("/auth/**").permitAll()
              .antMatchers("/actuator/**").permitAll()
              .antMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
              .anyRequest().authenticated()
              .and()
              .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
              .and()
              .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
  
          return http.build();
      }
  }
  ```

**Data Transfer Objects**:
- **src/main/java/com/guardix/backend/dto/AppScanRequest.java**: Request model
  ```java
  package com.guardix.backend.dto;
  
  import lombok.Data;
  
  import javax.validation.constraints.NotBlank;
  import javax.validation.constraints.NotNull;
  import java.util.List;
  
  @Data
  public class AppScanRequest {
      @NotBlank
      private String packageName;
      
      @NotNull
      private AppFeatures features;
      
      @Data
      public static class AppFeatures {
          private List<String> permissions;
          private List<String> apiCalls;
          private List<String> behaviors;
      }
  }
  ```

#### Architecture
**Java-Python Hybrid Structure**:
- **Java Backend**: REST API, authentication, business logic
- **Python ML Services**: Model inference via gRPC
- **Config Server**: Centralized configuration

#### API Endpoints
- `/malware/analyze`: App behavior analysis
- `/phishing/check`: URL/text phishing detection
- `/auth/biometric`: Biometric authentication
- `/intrusion/detect`: SSE/WebSocket for real-time alerts
- `/actuator/metrics`: Monitoring endpoints

#### Security & Enterprise Features
- Spring Security with JWT
- Role-based access control
- API documentation with OpenAPI
- Monitoring with Spring Actuator
- Rate limiting and request throttling

## Integration Points

### Mobile-Backend Integration
- **API Communication**: Retrofit client in Android app connects to backend REST APIs
- **Authentication Flow**: JWT token obtained via `/auth/login` and used in subsequent requests
- **Real-time Features**: WebSocket/SSE connections for intrusion alerts and monitoring
- **Offline Capabilities**: Lightweight models can be deployed on-device with server synchronization

### ML Model Deployment
- **Model Formats**: ONNX, TensorFlow SavedModel, scikit-learn joblib
- **Versioning**: Models tracked with version metadata
- **Continuous Learning**: Models updated with new data while maintaining version history
- **Federated Updates**: Privacy-preserving model improvements from device telemetry