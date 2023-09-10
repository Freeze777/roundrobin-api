package com.example.roundrobinserver.core.models;

import lombok.Getter;

public class ServerStats {
    private volatile long totalRequests = 1L;

    @Getter
    private volatile double successRate = 1.0;

    public void updateStats(boolean isSuccess) {
        this.successRate = (this.successRate * this.totalRequests + (isSuccess ? 1.0 : 0.0)) / (this.totalRequests + 1.0);
        this.totalRequests++;
    }
}
