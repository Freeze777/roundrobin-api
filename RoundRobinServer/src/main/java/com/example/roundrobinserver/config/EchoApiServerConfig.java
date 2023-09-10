package com.example.roundrobinserver.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class EchoApiServerConfig {
    @Value("${echo.server.list}")
    @Getter
    private List<String> servers;

    @Value("${echo.server.backoffms}")
    @Getter
    private int backoffTimeMs;

    @Value("${echo.server.retries}")
    @Getter
    private int retries;

    @Value("${echo.server.backoffmultiplier}")
    @Getter
    private int backoffMultiplier;
}
