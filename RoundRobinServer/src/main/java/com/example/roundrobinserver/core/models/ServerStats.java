package com.example.roundrobinserver.core.models;

import lombok.Getter;
import lombok.Setter;


public class ServerStats {
    @Setter
    @Getter
    private volatile long totalRequests = 1L;
    @Getter
    private volatile double successRate = 1.0;
    @Getter
    private volatile long lastUpdatedWhen = 0L;

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
        this.lastUpdatedWhen = System.currentTimeMillis();
    }
}
