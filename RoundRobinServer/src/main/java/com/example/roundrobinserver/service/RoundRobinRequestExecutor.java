package com.example.roundrobinserver.service;

import com.example.roundrobinserver.config.EchoApiConfig;
import com.example.roundrobinserver.service.models.EchoServerResponse;
import com.example.roundrobinserver.service.models.IRequestExecutor;
import com.example.roundrobinserver.service.models.ServerStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static com.example.roundrobinserver.service.utils.HttpUtils.*;

@Service
public class RoundRobinRequestExecutor implements IRequestExecutor {
    private final EchoApiConfig echoApiConfig;
    private final RestTemplate restTemplate;
    private final AtomicLong requestCounter = new AtomicLong(0);
    private final Map<String, ServerStats> serverSuccessRate = new ConcurrentHashMap<>();

    @Autowired
    public RoundRobinRequestExecutor(EchoApiConfig echoApiConfig, RestTemplate restTemplate) {
        this.echoApiConfig = echoApiConfig;
        this.restTemplate = restTemplate;
    }

    @Override
    public EchoServerResponse executeRequest(String requestBody) {
        return executeWithRetryAndBackoff(requestBody, this::executeRequestHelper);
    }

    private EchoServerResponse executeWithRetryAndBackoff(String requestBody, Function<String, EchoServerResponse> executorFunction) {
        int retries = echoApiConfig.getRetries();
        int backoff = echoApiConfig.getBackoffMs();
        EchoServerResponse response = null;
        while (retries > 0) {
            response = executorFunction.apply(requestBody);
            if (isSuccessful(response.getStatusCode()) || !isRetryableError(response.getStatusCode())) {
                updateServerStats(response, true);
                return response;
            }
            retries--;
            backoff *= 2;
            updateServerStats(response, false);
            try {
                Thread.sleep(backoff);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return response;
    }

    private EchoServerResponse executeRequestHelper(String requestBody) {
        String server = getNextServer();
        if (isAvailable(server)) {
            return EchoServerResponse.builder()
                    .statusCode(HttpStatus.SERVICE_UNAVAILABLE)
                    .errorMessage(Optional.of("Service Unavailable"))
                    .upstreamServerName(server)
                    .build();
        }
        String endpoint = String.format("http://%s/echo", server);
        HttpEntity<String> request = buildRequest(requestBody);
        try {
            ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.POST, request, String.class);
            return EchoServerResponse.builder()
                    .statusCode(response.getStatusCode())
                    .responseBody(response.getBody())
                    .errorMessage(Optional.empty())
                    .upstreamServerName(server)
                    .build();
        } catch (HttpClientErrorException ex) {
            return EchoServerResponse.builder()
                    .statusCode(ex.getStatusCode())
                    .responseBody(ex.getResponseBodyAsString())
                    .errorMessage(Optional.of(ex.getMessage()))
                    .upstreamServerName(server)
                    .build();
        } catch (Exception ex) {
            return EchoServerResponse.builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
                    .errorMessage(Optional.of(ex.getMessage()))
                    .upstreamServerName(server)
                    .build();
        }
    }

    private boolean isAvailable(String server) {
        return serverSuccessRate.containsKey(server) && serverSuccessRate.get(server).getSuccessRate() < echoApiConfig.getMinSuccessRate();
    }

    private void updateServerStats(EchoServerResponse response, boolean isSuccess) {
        serverSuccessRate.compute(response.getUpstreamServerName(), (k, v) -> {
            if (v == null) return new ServerStats();
            ServerStats stats = serverSuccessRate.get(k);
            stats.updateStats(isSuccess);
            return stats;
        });
    }

    private String getNextServer() {
        int nextIndex = (int) (requestCounter.getAndIncrement() % echoApiConfig.getServers().size());
        return echoApiConfig.getServers().get(nextIndex);
    }
}
