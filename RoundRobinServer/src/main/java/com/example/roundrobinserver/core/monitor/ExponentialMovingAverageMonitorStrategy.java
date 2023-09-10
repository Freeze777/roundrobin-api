package com.example.roundrobinserver.core.monitor;

import com.example.roundrobinserver.core.models.IServerMonitorStrategy;
import com.example.roundrobinserver.core.models.ServerStats;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExponentialMovingAverageMonitorStrategy implements IServerMonitorStrategy {
    private final Map<String, ServerStats> serverSuccessRate = new ConcurrentHashMap<>();
    private final double minSuccessRate;
    private final static double ALPHA = 0.8;

    public ExponentialMovingAverageMonitorStrategy(double minSuccessRate) {
        this.minSuccessRate = minSuccessRate;
    }

    @Override
    public double getServerSuccessRate(String server) {
        return serverSuccessRate.get(server).getSuccessRate();
    }

    @Override
    public void updateServerStats(String server, boolean isSuccess) {
        serverSuccessRate.compute(server, (k, v) -> {
            if (v == null) return new ServerStats();
            var stats = serverSuccessRate.get(k);
            stats.setSuccessRate(ALPHA * stats.getSuccessRate() + (isSuccess ? (1.0 - ALPHA) : 0.0));
            return stats;
        });
    }

    @Override
    public boolean isUnhealthy(String server) {
        return serverSuccessRate.containsKey(server) && getServerSuccessRate(server) <= minSuccessRate;
    }
}
