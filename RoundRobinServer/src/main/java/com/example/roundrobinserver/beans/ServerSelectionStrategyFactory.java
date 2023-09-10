package com.example.roundrobinserver.beans;

import com.example.roundrobinserver.core.RoundRobinServerSelectionStrategy;
import com.example.roundrobinserver.core.models.IServerSelectionStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ServerSelectionStrategyFactory {

    @Value("${echo.server.list}")
    private List<String> servers;

    @Bean
    public IServerSelectionStrategy getServerSelectionStrategy() {
        return new RoundRobinServerSelectionStrategy(servers);
    }
}
