package com.example.roundrobinserver.core.models;

import com.example.roundrobinserver.service.models.EchoServerResponse;

public interface IServerMonitorStrategy {
    double getServerSuccessRate(String server);

    void updateServerStats(EchoServerResponse response, boolean isSuccess);

    boolean isUnhealthy(String server);
}
