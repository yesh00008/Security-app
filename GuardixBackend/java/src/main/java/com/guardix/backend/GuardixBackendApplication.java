package com.guardix.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot application class for Guardix Backend
 * 
 * This enterprise-grade backend provides AI-powered security services
 * for the Android mobile application through REST APIs and gRPC integration
 * with Python ML services.
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
public class GuardixBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(GuardixBackendApplication.class, args);
    }
}