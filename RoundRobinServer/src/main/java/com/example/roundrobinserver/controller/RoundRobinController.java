package com.example.roundrobinserver.controller;

import com.example.roundrobinserver.service.models.IRoundRobinService;
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

    public IRoundRobinService roundRobinService;

    @Autowired
    public RoundRobinController(IRoundRobinService roundRobinService) {
        this.roundRobinService = roundRobinService;
    }

    @PostMapping("/roundrobin")
    public ResponseEntity<String> echo(@RequestBody String message) {
        var response = roundRobinService.routeRequest(message);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Upstream-Server", response.getUpstreamServerName());
        response.getErrorMessage().ifPresent(errorMessage -> headers.add("X-Error-Message", errorMessage));
        return new ResponseEntity<>(response.getResponseBody(), headers, response.getStatusCode());
    }

}
