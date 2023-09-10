package com.example.roundrobinserver.core.models;

public interface IServerMonitorStrategy {
    double getServerSuccessRate(String server);

    void updateServerStats(String server, boolean isSuccess);

    boolean isUnhealthy(String server);
}
