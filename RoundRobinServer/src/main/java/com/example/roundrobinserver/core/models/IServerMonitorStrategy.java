package com.example.roundrobinserver.core.models;

import java.util.Map;

public interface IServerMonitorStrategy {
    double getServerSuccessRate(String server);

    void updateServerStats(String server, boolean isSuccess);

    boolean isUnhealthy(String server);

    Map<String, ServerStats> getServerSuccessRates();
}
