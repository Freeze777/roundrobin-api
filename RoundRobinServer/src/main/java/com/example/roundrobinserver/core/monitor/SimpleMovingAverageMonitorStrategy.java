package com.example.roundrobinserver.core.monitor;

import com.example.roundrobinserver.core.models.IServerMonitorStrategy;
import com.example.roundrobinserver.core.models.ServerStats;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleMovingAverageMonitorStrategy implements IServerMonitorStrategy {
    private final Map<String, ServerStats> serverSuccessRate = new ConcurrentHashMap<>();
    private final double minSuccessRate;

    public SimpleMovingAverageMonitorStrategy(double minSuccessRate) {
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
            if(isUnhealthy(server)) return v;
            var stats = serverSuccessRate.get(k);
            stats.setSuccessRate((stats.getSuccessRate() * stats.getTotalRequests() + (isSuccess ? 1.0 : 0.0)) / (stats.getTotalRequests() + 1.0));
            stats.setTotalRequests(stats.getTotalRequests() + 1);
            return stats;
        });
    }

    @Override
    public boolean isUnhealthy(String server) {
        return serverSuccessRate.containsKey(server) && getServerSuccessRate(server) <= minSuccessRate;
    }

    @Override
    public Map<String, ServerStats> getServerSuccessRates() {
        return serverSuccessRate;
    }
}
