package com.example.roundrobinserver.core.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerStats {
    private volatile long totalRequests = 1L;
    private volatile double successRate = 1.0;
}
