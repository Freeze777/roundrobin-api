package com.example.roundrobinserver.beans;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateFactory {
    @Value("${upstream.server.timeoutms}")
    @Getter
    private int timeoutMs;

    @Bean
    public RestTemplate getInstance() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(timeoutMs);
        factory.setConnectTimeout(timeoutMs);
        return new RestTemplate(factory);
    }

}
