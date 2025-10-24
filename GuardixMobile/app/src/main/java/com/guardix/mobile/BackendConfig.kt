package com.guardix.mobile

object BackendConfig {
    /**
     * Backend Configuration
     * 
     * Choose your backend by uncommenting one of the options below:
     */
    
    // OPTION 1: Python FastAPI Backend (Default - Recommended)
    object Python {
        const val BASE_URL = "http://10.0.2.2:8000/"  // For Android Emulator
        // const val BASE_URL = "http://192.168.1.100:8000/"  // For Physical Device (Replace with your IP)
        // const val BASE_URL = "https://api.guardix.com/"  // For Production
        const val PORT = 8000
        const val SUPPORTS_WEBSOCKET = true
        const val SUPPORTS_REST = true
    }
    
    // OPTION 2: Java Spring Boot Backend
    object Java {
        const val BASE_URL = "http://10.0.2.2:8080/"  // For Android Emulator
        // const val BASE_URL = "http://192.168.1.100:8080/"  // For Physical Device (Replace with your IP)
        // const val BASE_URL = "https://api.guardix.com/"  // For Production
        const val PORT = 8080
        const val SUPPORTS_GRPC = true
        const val SUPPORTS_REST = true
    }
    
    // OPTION 3: Node.js Backend (To be implemented)
    object NodeJS {
        const val BASE_URL = "http://10.0.2.2:3000/"
        const val PORT = 3000
        const val SUPPORTS_WEBSOCKET = true
        const val SUPPORTS_REST = true
    }
    
    /**
     * Active Configuration
     * Change this to switch between backends
     */
    enum class BackendType {
        PYTHON,
        JAVA,
        NODEJS
    }
    
    // Set your active backend here
    val ACTIVE_BACKEND = BackendType.PYTHON
    
    // Get current base URL based on active backend
    val API_BASE_URL: String
        get() = when (ACTIVE_BACKEND) {
            BackendType.PYTHON -> Python.BASE_URL
            BackendType.JAVA -> Java.BASE_URL
            BackendType.NODEJS -> NodeJS.BASE_URL
        }
    
    val WEBSOCKET_BASE_URL: String
        get() = API_BASE_URL.replace("http://", "ws://").replace("https://", "wss://")
    
    /**
     * Network Configuration for Physical Devices
     * 
     * Steps to find your computer's IP address:
     * 
     * Windows:
     *   1. Open Command Prompt
     *   2. Run: ipconfig
     *   3. Look for "IPv4 Address" under your active network adapter
     *   4. Example: 192.168.1.100
     * 
     * macOS/Linux:
     *   1. Open Terminal
     *   2. Run: ifconfig (Mac) or ip addr (Linux)
     *   3. Look for inet address
     *   4. Example: 192.168.1.100
     * 
     * Then update the BASE_URL above with your IP address:
     *   const val BASE_URL = "http://YOUR_IP:PORT/"
     */
    
    /**
     * Environment Configuration
     */
    object Environment {
        const val IS_DEVELOPMENT = true
        const val ENABLE_LOGGING = true
        const val CONNECT_TIMEOUT_SECONDS = 30L
        const val READ_TIMEOUT_SECONDS = 30L
        const val WRITE_TIMEOUT_SECONDS = 30L
    }
}
