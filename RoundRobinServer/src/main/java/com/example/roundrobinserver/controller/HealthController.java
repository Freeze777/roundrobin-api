package com.example.roundrobinserver.controller;

import com.example.roundrobinserver.core.models.IServerMonitorStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final IServerMonitorStrategy serverMonitorStrategy;

    @Autowired
    public HealthController(IServerMonitorStrategy serverMonitorStrategy) {
        this.serverMonitorStrategy = serverMonitorStrategy;
    }

    @GetMapping("/healthstats")
    public ResponseEntity<String> health() {
        return new ResponseEntity<>(serverMonitorStrategy.getServerSuccessRates().toString(), HttpStatus.OK);
    }
}
