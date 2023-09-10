package com.example.roundrobinserver.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RetryConfig {
    @Value("${echo.server.retry.backoffms}")
    @Getter
    private int backoffTimeMs;

    @Value("${echo.server.retry.retries}")
    @Getter
    private int retries;

    @Value("${echo.server.retry.backoffmultiplier}")
    @Getter
    private int backoffMultiplier;
}
