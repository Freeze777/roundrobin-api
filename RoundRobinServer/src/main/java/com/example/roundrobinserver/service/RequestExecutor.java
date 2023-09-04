package com.example.roundrobinserver.service;

import com.example.roundrobinserver.config.EchoApiConfig;
import com.example.roundrobinserver.service.models.IRequestExecutor;
import com.example.roundrobinserver.service.models.EchoServerResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RequestExecutor implements IRequestExecutor {
    private final List<String> onlineServers;
    private final RestTemplate restTemplate;
    private final AtomicInteger requestCount = new AtomicInteger(0);

    @Autowired
    public RequestExecutor(EchoApiConfig echoApiConfig, RestTemplate restTemplate) {
        this.onlineServers = new ArrayList<>(echoApiConfig.getServers());
        this.restTemplate = restTemplate;
    }

    @Override
    public EchoServerResponse executeRequest(String requestBody) {
        String server = onlineServers.get(requestCount.getAndIncrement() % onlineServers.size());
        String url = String.format("http://%s/echo", server);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            HttpStatusCode statusCode = response.getStatusCode();
            return EchoServerResponse.builder()
                    .statusCode(statusCode)
                    .responseBody(response.getBody())
                    .errorMessage(Optional.empty())
                    .build();
        } catch (HttpClientErrorException ex) { // Handle 4xx errors
            return EchoServerResponse.builder()
                    .statusCode(ex.getStatusCode())
                    .errorMessage(Optional.of(ex.getResponseBodyAsString()))
                    .build();
        } catch (Exception ex) {
            return EchoServerResponse.builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
                    .errorMessage(Optional.of(ex.getMessage()))
                    .build();
        }
    }
}
