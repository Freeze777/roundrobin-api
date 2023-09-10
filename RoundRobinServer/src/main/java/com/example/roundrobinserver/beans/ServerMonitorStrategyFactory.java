package com.example.roundrobinserver.beans;

import com.example.roundrobinserver.core.monitor.SimpleMovingAverageMonitorStrategy;
import com.example.roundrobinserver.core.models.IServerMonitorStrategy;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServerMonitorStrategyFactory {

    @Value("${echo.server.health.minsuccessrate}")
    @Getter
    private double minSuccessRate;

    @Bean
    public IServerMonitorStrategy getServerMonitorStrategy() {
        return new SimpleMovingAverageMonitorStrategy(minSuccessRate);
    }
}
