package com.guardix.backend.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

/**
 * gRPC configuration for connecting to Python ML services
 * Manages channels and connection settings for all ML services
 */
@Configuration
@Slf4j
public class GrpcConfig {

    @Value("${grpc.malware.host:localhost}")
    private String malwareHost;

    @Value("${grpc.malware.port:50051}")
    private int malwarePort;

    @Value("${grpc.phishing.host:localhost}")
    private String phishingHost;

    @Value("${grpc.phishing.port:50052}")
    private int phishingPort;

    @Value("${grpc.biometric.host:localhost}")
    private String biometricHost;

    @Value("${grpc.biometric.port:50053}")
    private int biometricPort;

    @Value("${grpc.intrusion.host:localhost}")
    private String intrusionHost;

    @Value("${grpc.intrusion.port:50054}")
    private int intrusionPort;

    @Value("${grpc.connection.timeout:30}")
    private int connectionTimeoutSeconds;

    @Value("${grpc.keepalive.time:30}")
    private int keepAliveTimeSeconds;

    @Value("${grpc.keepalive.timeout:5}")
    private int keepAliveTimeoutSeconds;

    private ManagedChannel malwareChannel;
    private ManagedChannel phishingChannel;
    private ManagedChannel biometricChannel;
    private ManagedChannel intrusionChannel;

    @Bean
    public ManagedChannel malwareChannel() {
        log.info("Creating gRPC channel for Malware service at {}:{}", malwareHost, malwarePort);
        malwareChannel = createChannel(malwareHost, malwarePort);
        return malwareChannel;
    }

    @Bean
    public ManagedChannel phishingChannel() {
        log.info("Creating gRPC channel for Phishing service at {}:{}", phishingHost, phishingPort);
        phishingChannel = createChannel(phishingHost, phishingPort);
        return phishingChannel;
    }

    @Bean
    public ManagedChannel biometricChannel() {
        log.info("Creating gRPC channel for Biometric service at {}:{}", biometricHost, biometricPort);
        biometricChannel = createChannel(biometricHost, biometricPort);
        return biometricChannel;
    }

    @Bean
    public ManagedChannel intrusionChannel() {
        log.info("Creating gRPC channel for Intrusion service at {}:{}", intrusionHost, intrusionPort);
        intrusionChannel = createChannel(intrusionHost, intrusionPort);
        return intrusionChannel;
    }

    private ManagedChannel createChannel(String host, int port) {
        return ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext() // Use TLS in production
                .keepAliveTime(keepAliveTimeSeconds, TimeUnit.SECONDS)
                .keepAliveTimeout(keepAliveTimeoutSeconds, TimeUnit.SECONDS)
                .keepAliveWithoutCalls(true)
                .maxInboundMessageSize(4 * 1024 * 1024) // 4MB max message size
                .build();
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down gRPC channels...");
        
        shutdownChannel(malwareChannel, "Malware");
        shutdownChannel(phishingChannel, "Phishing");
        shutdownChannel(biometricChannel, "Biometric");
        shutdownChannel(intrusionChannel, "Intrusion");
    }

    private void shutdownChannel(ManagedChannel channel, String serviceName) {
        if (channel != null && !channel.isShutdown()) {
            try {
                channel.shutdown();
                if (!channel.awaitTermination(connectionTimeoutSeconds, TimeUnit.SECONDS)) {
                    log.warn("{} channel did not terminate gracefully, forcing shutdown", serviceName);
                    channel.shutdownNow();
                }
                log.info("{} channel shut down successfully", serviceName);
            } catch (InterruptedException e) {
                log.error("Error shutting down {} channel", serviceName, e);
                Thread.currentThread().interrupt();
            }
        }
    }
}