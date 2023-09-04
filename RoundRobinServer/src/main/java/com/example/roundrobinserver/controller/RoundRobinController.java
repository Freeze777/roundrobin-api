package com.example.roundrobinserver.controller;

import com.example.roundrobinserver.service.models.EchoServerResponse;
import com.example.roundrobinserver.service.models.IRoundRobinService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public EchoServerResponse echo(@RequestBody String message) {
        return roundRobinService.routeRequest(message);
    }

}
