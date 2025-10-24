# Guardix Mobile Security App

A modern Android security application built with Jetpack Compose, replicating the clean and futuristic design of Vivo iManager. This app provides comprehensive device security features with an intuitive and visually appealing interface.

## 🚀 Features

### Main Dashboard
- **Circular Security Indicator**: Interactive scanning button with real-time progress animation
- **Security Score**: Visual representation of device security status (0-100%)
- **Quick Action Cards**: Easy access to essential security tools
- **Activity Feed**: Recent security events and system activities

### Security Tools
- **Malware Scanner**: Device scan backed by Guardix backend ML classifiers
- **Phishing Protection**: Real-time URL analysis using backend NLP model
- **Network Monitor**: Sends telemetry to backend IDS for anomaly scoring
- **Model Insights**: Inspect active backend models and lightweight profiles
- **App Permissions**: Review and manage application permissions

### Performance Tools
- **Memory Cleaner**: Free up RAM and optimize performance
- **Storage Cleaner**: Remove junk files and cache data
- **Battery Optimizer**: Extend battery life and monitor health
- **CPU Monitor**: Real-time CPU usage and temperature monitoring

### Privacy Tools
- **Biometric Setup**: Configure fingerprint and face unlock
- **App Lock**: Secure sensitive apps with PIN or biometrics
- **Privacy Cleaner**: Clear browsing history and private data
- **Location Guard**: Monitor and control location access

## 🎨 Design Features

### Theme & Colors
- **Primary Colors**: Light blue (#4FC3F7) and cyan (#00BCD4)
- **Background**: Clean white with subtle blue gradients
- **Accent Colors**: Subtle grays for secondary elements
- **Status Colors**: Green for success, orange for warnings, red for errors

### UI Components
- **Neumorphic Cards**: Soft shadows and elevated surfaces
- **Rounded Corners**: 12-16dp radius for modern appearance
- **Gradient Backgrounds**: Soft blue-to-white transitions
- **Material Icons**: Consistent iconography throughout the app

### Animations
- **Smooth Transitions**: Fluid navigation between screens
- **Progress Animations**: Engaging scanning and loading states
- **Interactive Feedback**: Bouncy click animations and ripple effects
- **Staggered Animations**: Sequential appearance of list items

## 🛠️ Tech Stack

- **Framework**: Jetpack Compose (latest stable version)
- **Language**: Kotlin
- **Architecture**: MVVM with Compose
- **Navigation**: Jetpack Navigation Compose
- **UI**: Material Design 3
- **Dependencies**:
  - Compose BOM 2024.09.00
  - Navigation Compose 2.8.0
  - Material 3 & Icons Extended
  - Retrofit + Moshi + OkHttp (backend integration)
  - Kotlin Coroutines

## 📱 Responsive Design

The app adapts to different screen sizes:
- **Compact (< 600dp)**: Standard phone layout with 2-column grids
- **Medium (600-840dp)**: Enhanced spacing with 3-column grids
- **Expanded (> 840dp)**: Tablet layout with 4-column grids and increased padding

## 🏗️ Project Structure

```
app/src/main/java/com/guardix/mobile/
├── MainActivity.kt                 # Entry point
├── GuardixApp.kt                  # Main app navigation
├── ui/
│   ├── theme/                     # Design system
│   │   ├── Color.kt              # Color palette
│   │   ├── Theme.kt              # Material theme setup
│   │   └── Type.kt               # Typography definitions
│   ├── components/                # Reusable UI components
│   │   ├── Cards.kt              # Neumorphic and gradient cards
│   │   └── CircularIndicator.kt  # Security score indicator
│   ├── screens/                   # Main app screens
│   │   ├── HomeScreen.kt         # Security dashboard
│   │   ├── ToolsScreen.kt        # Security tools
│   │   └── SettingsScreen.kt     # App settings
│   ├── animations/                # Animation utilities
│   │   └── Animations.kt         # Custom animations
│   └── utils/                     # Utility functions
│       └── ResponsiveUtils.kt    # Responsive design helpers
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog | 2023.1.1 or later
- JDK 8 or higher
- Android SDK API level 24 (Android 7.0) or higher

### Installation
1. Clone or download the project to your local machine
2. Open Android Studio
3. Select "Open an existing project" and navigate to the `GuardixMobile` folder
4. Wait for Gradle sync to complete
5. Run the project on an emulator or physical device

### Build Commands
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

## 📋 Development Status

### Completed Features ✅
- [x] Complete project structure with Gradle configuration
- [x] Material Design 3 theme with custom color scheme
- [x] Neumorphic card components and design system
- [x] Home screen with circular security indicator
- [x] Bottom navigation with three main tabs
- [x] Tools screen with categorized security features
- [x] Settings screen with organized preferences
- [x] Smooth animations and transitions
- [x] Responsive design for different screen sizes

### Future Enhancements 🔮
- [x] Integration with real security APIs
- [x] Biometric authentication verification
- [x] Real-time malware scanning engine
- [x] Network traffic monitoring
- [ ] Cloud backup and sync
- [ ] Multi-language support
- [ ] Dark mode theme variations

## 🤝 Contributing

This is a frontend-only implementation with mock data. To contribute:
1. Fork the repository
2. Create a feature branch
3. Implement your changes
4. Test on multiple screen sizes
5. Submit a pull request

## 📄 License

This project is created for educational and demonstration purposes. The design is inspired by Vivo iManager while implementing original code and components.

## 🔗 Backend Integration

The mobile app communicates with the Guardix FastAPI backend bundled in this repo. Default configuration:

- Base URL: `http://10.0.2.2:8000/` (set via `BuildConfig.API_BASE_URL` for the Android emulator)
- Default user id: `guardix-device` (`BuildConfig.DEFAULT_USER_ID`)

Active API calls from the app:

- `POST /auth/login` – obtain JWT for authenticated calls
- `POST /scan/apk` – classify installed apps with the malware model
- `POST /scan/phishing` – analyse URLs/text through the phishing model
- `POST /auth/biometric` – validate behavioural biometric samples
- `POST /monitor/ids` – send telemetry to the IDS
- `GET /models/` – display lightweight model metadata

To run end-to-end:

1. Start the backend (`uvicorn app.main:app --reload --host 0.0.0.0 --port 8000` inside `GuardixBackend/python`).
2. Ensure the device can reach the backend host (adjust `API_BASE_URL` in `app/build.gradle.kts` if necessary).
3. Launch the Android app – Malware Scanner, Phishing Protection, Network Monitor, Biometric Setup, and Model Insights will now use live backend inference.

## 📞 Support

For support or questions about this implementation:
- Check the code documentation
- Review the Material Design 3 guidelines
- Consult Jetpack Compose documentation
- Test on multiple devices and screen sizes

---

**Built with ❤️ using Jetpack Compose and Material Design 3**
