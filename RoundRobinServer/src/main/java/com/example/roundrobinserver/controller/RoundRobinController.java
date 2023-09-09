package com.example.roundrobinserver.controller;

import com.example.roundrobinserver.service.models.IRoundRobinService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Component
public class RoundRobinController {
    private static final Logger logger = LoggerFactory.getLogger(RoundRobinController.class);
    public IRoundRobinService roundRobinService;

    @Autowired
    public RoundRobinController(IRoundRobinService roundRobinService) {
        this.roundRobinService = roundRobinService;
    }

    @PostMapping("/api/echo")
    public ResponseEntity<String> echo(@RequestBody String request) {
        var response = roundRobinService.routeRequest(request);
        logger.info("Request:{} was served by server:{} with {} status", request, response.getUpstreamServerName(), response.getStatusCode());
        return new ResponseEntity<>(response.getResponseBody(), new HttpHeaders(), response.getStatusCode());
    }
}
