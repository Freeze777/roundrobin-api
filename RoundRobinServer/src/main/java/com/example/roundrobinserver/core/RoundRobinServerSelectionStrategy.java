package com.example.roundrobinserver.core;

import com.example.roundrobinserver.core.models.IServerSelectionStrategy;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class RoundRobinServerSelectionStrategy implements IServerSelectionStrategy {
    private final AtomicLong requestCounter = new AtomicLong(0);
    private final List<String> servers;

    public RoundRobinServerSelectionStrategy(List<String> servers) {
        this.servers = servers;
    }

    @Override
    public String getNextServer() {
        int nextIndex = (int) (requestCounter.getAndIncrement() % servers.size());
        nextIndex = nextIndex < 0 ? nextIndex + servers.size() : nextIndex;
        return servers.get(nextIndex);
    }
}
