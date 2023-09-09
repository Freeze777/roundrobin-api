package com.example.roundrobinserver.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class EchoApiConfig {
    @Value("${echo.server.list}")
    @Getter
    private List<String> servers;

    @Value("${echo.server.backoffms}")
    @Getter
    private int backoffMs;

    @Value("${echo.server.retries}")
    @Getter
    private int retries;

    @Value("${echo.server.minsuccessrate}")
    @Getter
    private double minSuccessRate;
}
