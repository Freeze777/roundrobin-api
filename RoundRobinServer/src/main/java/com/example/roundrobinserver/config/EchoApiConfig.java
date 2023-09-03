package com.example.roundrobinserver.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EchoApiConfig {
    @Value("${echo-server-list}")
    @Getter
    private List<String> servers;
}
