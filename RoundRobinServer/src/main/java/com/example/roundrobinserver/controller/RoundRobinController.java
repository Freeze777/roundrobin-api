package com.example.roundrobinserver.controller;

import com.example.roundrobinserver.service.RoundRobinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Component
public class RoundRobinController {

    public RoundRobinService roundRobinService;

    @Autowired
    public RoundRobinController(RoundRobinService roundRobinService) {
        this.roundRobinService = roundRobinService;
    }

    @PostMapping("/roundrobin")
    public String echo(@RequestBody String message) {
        return roundRobinService.getServer().toString();
    }

}
