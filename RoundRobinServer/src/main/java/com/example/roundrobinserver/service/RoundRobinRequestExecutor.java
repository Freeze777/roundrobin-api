package com.example.roundrobinserver.service;

import com.example.roundrobinserver.config.EchoApiConfig;
import com.example.roundrobinserver.service.models.EchoServerResponse;
import com.example.roundrobinserver.service.models.IRequestExecutor;
import com.example.roundrobinserver.service.models.ServerStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(RoundRobinRequestExecutor.class);
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
    public EchoServerResponse executeRequest(String request) {
        return executeWithRetryAndBackoff(request, this::executeRequestHelper);
    }

    private EchoServerResponse executeWithRetryAndBackoff(String requestBody, Function<String, EchoServerResponse> executorFunction) {
        var retries = echoApiConfig.getRetries();
        var backoffTimeMs = echoApiConfig.getBackoffTimeMs();
        EchoServerResponse response = null;
        while (retries > 0) {
            response = executorFunction.apply(requestBody);
            if (isSuccessful(response.getStatusCode()) || !isRetryableError(response.getStatusCode())) {
                logger.info("Request to server {} was completed with {} status", response.getUpstreamServerName(), response.getStatusCode());
                updateServerStats(response, true);
                return response;
            }
            retries--;
            backoffTimeMs *= echoApiConfig.getBackoffMultiplier();
            updateServerStats(response, false);
            logger.error("Request to server {} with success-rate={}% failed for {}. Retrying in {} ms",
                    response.getUpstreamServerName(), getServerSuccessRate(response.getUpstreamServerName()),
                    requestBody, backoffTimeMs);
            try {
                Thread.sleep(backoffTimeMs);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return response;
    }

    private EchoServerResponse executeRequestHelper(String requestBody) {
        var server = getNextServer();
        if (isUnhealthy(server)) {
            logger.warn("Server {} is unhealthy with success-rate={}%", server, getServerSuccessRate(server));
            return EchoServerResponse.builder()
                    .statusCode(HttpStatus.SERVICE_UNAVAILABLE)
                    .errorMessage(Optional.of("Service Unavailable"))
                    .upstreamServerName(server)
                    .build();
        }
        var endpoint = String.format("http://%s/echo", server);
        var request = buildRequest(requestBody);
        try {
            var response = restTemplate.exchange(endpoint, HttpMethod.POST, request, String.class);
            return EchoServerResponse.builder()
                    .statusCode(response.getStatusCode())
                    .responseBody(response.getBody())
                    .errorMessage(Optional.empty())
                    .upstreamServerName(server)
                    .build();
        } catch (HttpClientErrorException ex) {
            logger.error("Error while executing {} to server {}", request, server, ex);
            return EchoServerResponse.builder()
                    .statusCode(ex.getStatusCode())
                    .responseBody(ex.getResponseBodyAsString())
                    .errorMessage(Optional.of(ex.getMessage()))
                    .upstreamServerName(server)
                    .build();
        } catch (Exception ex) {
            logger.error("Error while executing {} to server {}", request, server, ex);
            return EchoServerResponse.builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
                    .errorMessage(Optional.of(ex.getMessage()))
                    .upstreamServerName(server)
                    .build();
        }
    }

    private boolean isUnhealthy(String server) {
        return serverSuccessRate.containsKey(server) && serverSuccessRate.get(server).getSuccessRate() <= echoApiConfig.getMinSuccessRate();
    }

    private void updateServerStats(EchoServerResponse response, boolean isSuccess) {
        serverSuccessRate.compute(response.getUpstreamServerName(), (k, v) -> {
            if (v == null) return new ServerStats();
            var stats = serverSuccessRate.get(k);
            stats.updateStats(isSuccess);
            return stats;
        });
    }

    private String getNextServer() {
        int nextIndex = (int) (requestCounter.getAndIncrement() % echoApiConfig.getServers().size());
        nextIndex = nextIndex < 0 ? nextIndex + echoApiConfig.getServers().size() : nextIndex;
        return echoApiConfig.getServers().get(nextIndex);
    }

    private double getServerSuccessRate(String server) {
        return serverSuccessRate.get(server).getSuccessRate() * 100.0;
    }
}
