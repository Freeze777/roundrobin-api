package com.example.roundrobinserver.service.models;

import lombok.Getter;

public class ServerStats {
    private volatile long totalRequests = 0L;

    @Getter
    private volatile double successRate = 0.0;

    public ServerStats(boolean isSuccess) {
        updateStats(isSuccess);
    }

    public void updateStats(boolean isSuccess) {
        this.successRate = (this.successRate * this.totalRequests + (isSuccess ? 1.0 : 0.0)) / (this.totalRequests + 1.0);
        this.totalRequests++;
    }
}
