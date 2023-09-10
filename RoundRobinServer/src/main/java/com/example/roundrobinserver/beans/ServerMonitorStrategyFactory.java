package com.example.roundrobinserver.beans;

import com.example.roundrobinserver.core.models.IServerMonitorStrategy;
import com.example.roundrobinserver.core.monitor.ExponentialMovingAverageMonitorStrategy;
import com.example.roundrobinserver.core.monitor.SimpleMovingAverageMonitorStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServerMonitorStrategyFactory {

    @Value("${echo.server.health.minsuccessrate}")
    private double minSuccessRate;

    @Value("${echo.server.health.monitor.strategy}")
    private String strategy;

    @Value("${echo.server.health.monitor.ema.alpha}")
    private double alpha;

    @Bean
    public IServerMonitorStrategy getServerMonitorStrategy() {
        switch (strategy) {
            case "EMA" -> {
                return new ExponentialMovingAverageMonitorStrategy(minSuccessRate, alpha);
            }
            case "SMA" -> {
                return new SimpleMovingAverageMonitorStrategy(minSuccessRate);
            }
            default -> throw new IllegalArgumentException("Invalid server monitor strategy");
        }
    }
}
