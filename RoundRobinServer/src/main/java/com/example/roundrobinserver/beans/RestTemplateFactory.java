package com.example.roundrobinserver.beans;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateFactory {
    @Bean
    public RestTemplate getInstance() {
        return new RestTemplate();
    }

}
