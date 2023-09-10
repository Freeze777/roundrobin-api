package com.example.roundrobinserver.core;

import com.example.roundrobinserver.core.models.IServerMonitorStrategy;
import com.example.roundrobinserver.core.models.ServerStats;
import com.example.roundrobinserver.service.models.EchoServerResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleMovingAverageMonitorStrategy implements IServerMonitorStrategy {
    private final Map<String, ServerStats> serverSuccessRate = new ConcurrentHashMap<>();
    private final double minSuccessRate;

    public SimpleMovingAverageMonitorStrategy(double minSuccessRate) {
        this.minSuccessRate = minSuccessRate;
    }

    public double getServerSuccessRate(String server) {
        return serverSuccessRate.get(server).getSuccessRate() * 100.0;
    }

    public void updateServerStats(EchoServerResponse response, boolean isSuccess) {
        serverSuccessRate.compute(response.getUpstreamServerName(), (k, v) -> {
            if (v == null) return new ServerStats();
            var stats = serverSuccessRate.get(k);
            stats.setSuccessRate((stats.getSuccessRate() * stats.getTotalRequests() + (isSuccess ? 1.0 : 0.0)) / (stats.getTotalRequests() + 1.0));
            stats.setTotalRequests(stats.getTotalRequests() + 1);
            return stats;
        });
    }

    public boolean isUnhealthy(String server) {
        return serverSuccessRate.containsKey(server) && serverSuccessRate.get(server).getSuccessRate() <= minSuccessRate;
    }
}
