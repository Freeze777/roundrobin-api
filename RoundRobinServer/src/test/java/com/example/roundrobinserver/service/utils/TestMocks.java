package com.example.roundrobinserver.service.utils;

import com.example.roundrobinserver.config.RetryConfig;
import com.example.roundrobinserver.core.selection.RoundRobinServerSelectionStrategy;
import com.example.roundrobinserver.core.monitor.SimpleMovingAverageMonitorStrategy;
import com.example.roundrobinserver.core.models.IServerMonitorStrategy;
import com.example.roundrobinserver.core.models.IServerSelectionStrategy;

import java.util.ArrayList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestMocks {
    public static RetryConfig getMockEchoApiConfig(int retries) {
        var echoApiConfig = mock(RetryConfig.class);
        when(echoApiConfig.getRetries()).thenReturn(retries);
        when(echoApiConfig.getBackoffTimeMs()).thenReturn(1);
        when(echoApiConfig.getBackoffMultiplier()).thenReturn(2);
        return echoApiConfig;
    }

    public static IServerMonitorStrategy getMockMonitorStrategy() {
        return new SimpleMovingAverageMonitorStrategy(0.1);
    }

    public static IServerSelectionStrategy getMockServerSelectionStrategy(int numApiServers) {
        var serverList = new ArrayList<String>();
        for (int i = 1; i <= numApiServers; i++) serverList.add(String.format("server%d", i));
        return new RoundRobinServerSelectionStrategy(serverList);
    }
}
