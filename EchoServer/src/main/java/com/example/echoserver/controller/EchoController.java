package com.example.echoserver.controller;


import com.example.echoserver.validation.JsonValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EchoController {
    @PostMapping("/echo")
    public ResponseEntity<String> echo(@RequestBody String message) {
        return JsonValidator.isValidJson(message) ? new ResponseEntity<>(message, HttpStatus.OK) :
                new ResponseEntity<>("[ERROR] : Invalid json payload", HttpStatus.BAD_REQUEST);
    }
}
