package com.example.echoserver.controller;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EchoController {
    @PostMapping("/echo")
    public String echo(@RequestBody String message) {
        return message;
    }
}
