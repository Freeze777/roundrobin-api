package com.example.roundrobinserver.service.models;

import lombok.Getter;

public class ServerStats {
    private volatile long totalRequests;

    @Getter
    private volatile double successRate;

    public ServerStats() {
        this.totalRequests = 1L;
        this.successRate = 1.0;
    }

    public void updateStats(boolean isSuccess) {
        this.successRate = (this.successRate * this.totalRequests + (isSuccess ? 1 : 0)) / (this.totalRequests + 1);
        this.totalRequests++;
    }
}
