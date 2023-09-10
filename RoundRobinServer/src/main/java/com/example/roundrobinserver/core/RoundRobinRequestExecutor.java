package com.example.roundrobinserver.core;

import com.example.roundrobinserver.config.RetryConfig;
import com.example.roundrobinserver.core.models.IRequestExecutor;
import com.example.roundrobinserver.core.models.IServerMonitorStrategy;
import com.example.roundrobinserver.core.models.IServerSelectionStrategy;
import com.example.roundrobinserver.service.models.EchoServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static com.example.roundrobinserver.utils.HttpUtils.buildRequest;

@Service
public class RoundRobinRequestExecutor implements IRequestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(RoundRobinRequestExecutor.class);
    private final RestTemplate restTemplate;
    private final IServerMonitorStrategy serverMonitor;
    private final IServerSelectionStrategy serverSelector;
    private final RetryWrapper retryWrapper;

    @Autowired
    public RoundRobinRequestExecutor(RetryConfig retryConfig,
                                     RestTemplate restTemplate, IServerMonitorStrategy serverMonitor,
                                     IServerSelectionStrategy serverSelectionStrategy) {
        this.restTemplate = restTemplate;
        this.serverMonitor = serverMonitor;
        this.serverSelector = serverSelectionStrategy;
        this.retryWrapper = new RetryWrapper(retryConfig, serverMonitor);
    }

    @Override
    public EchoServerResponse executeRequest(String request) {
        return retryWrapper.execute(request, this::executeRequestHelper);
    }

    private EchoServerResponse executeRequestHelper(String request) {
        var server = serverSelector.getNextServer();
        if (serverMonitor.isUnhealthy(server)) {
            logger.warn("Server {} is unhealthy with success-rate={}%", server, serverMonitor.getServerSuccessRate(server));
            return EchoServerResponse.builder().statusCode(HttpStatus.SERVICE_UNAVAILABLE).errorMessage(Optional.of("Service Unavailable")).upstreamServerName(server).build();
        }
        var endpoint = String.format("http://%s/echo", server);
        var requestEntity = buildRequest(request);
        try {
            var response = restTemplate.exchange(endpoint, HttpMethod.POST, requestEntity, String.class);
            return EchoServerResponse.builder().statusCode(response.getStatusCode()).responseBody(response.getBody()).errorMessage(Optional.empty()).upstreamServerName(server).build();
        } catch (HttpClientErrorException ex) {
            logger.error("Error while executing {} to server {}", requestEntity, server, ex);
            return EchoServerResponse.builder().statusCode(ex.getStatusCode()).responseBody(ex.getResponseBodyAsString()).errorMessage(Optional.of(ex.getMessage())).upstreamServerName(server).build();
        } catch (Exception ex) {
            logger.error("Error while executing {} to server {}", requestEntity, server, ex);
            return EchoServerResponse.builder().statusCode(HttpStatus.INTERNAL_SERVER_ERROR).errorMessage(Optional.of(ex.getMessage())).upstreamServerName(server).build();
        }
    }
}
