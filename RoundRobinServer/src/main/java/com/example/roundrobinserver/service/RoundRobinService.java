package com.example.roundrobinserver.service;


import com.example.roundrobinserver.config.EchoApiConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoundRobinService {

    private final EchoApiConfig echoApiConfig;

    @Autowired
    public RoundRobinService(EchoApiConfig echoApiConfig) {
        this.echoApiConfig = echoApiConfig;
    }

    public List<String> getServer() {
        return echoApiConfig.getServers();
    }
}
