package com.example.roundrobinserver.core;

import com.example.roundrobinserver.config.EchoApiServerConfig;
import com.example.roundrobinserver.core.models.IRequestExecutor;
import com.example.roundrobinserver.core.models.IServerMonitorStrategy;
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
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static com.example.roundrobinserver.utils.HttpUtils.*;

@Service
public class RoundRobinRequestExecutor implements IRequestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(RoundRobinRequestExecutor.class);
    private final EchoApiServerConfig echoApiServerConfig;
    private final RestTemplate restTemplate;
    private final AtomicLong requestCounter = new AtomicLong(0);
    private final IServerMonitorStrategy serverMonitor;

    @Autowired
    public RoundRobinRequestExecutor(EchoApiServerConfig echoApiServerConfig, RestTemplate restTemplate, IServerMonitorStrategy serverMonitor) {
        this.echoApiServerConfig = echoApiServerConfig;
        this.restTemplate = restTemplate;
        this.serverMonitor = serverMonitor;
    }

    @Override
    public EchoServerResponse executeRequest(String request) {
        return executeWithRetryAndBackoff(request, this::executeRequestHelper);
    }

    private EchoServerResponse executeWithRetryAndBackoff(String requestBody, Function<String, EchoServerResponse> executorFunction) {
        var retries = echoApiServerConfig.getRetries();
        var backoffTimeMs = echoApiServerConfig.getBackoffTimeMs();
        EchoServerResponse response = null;
        while (retries > 0) {
            response = executorFunction.apply(requestBody);
            if (isSuccessful(response.getStatusCode()) || !isRetryableError(response.getStatusCode())) {
                logger.info("Request to server {} was completed with {} status", response.getUpstreamServerName(), response.getStatusCode());
                serverMonitor.updateServerStats(response, true);
                return response;
            }
            retries--;
            backoffTimeMs *= echoApiServerConfig.getBackoffMultiplier();
            serverMonitor.updateServerStats(response, false);
            logger.error("Request to server {} with success-rate={}% failed for {}. Retrying in {} ms",
                    response.getUpstreamServerName(), serverMonitor.getServerSuccessRate(response.getUpstreamServerName()),
                    requestBody, backoffTimeMs);
            try {
                Thread.sleep(backoffTimeMs);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return response;
    }

    private EchoServerResponse executeRequestHelper(String request) {
        var server = getNextServer();
        if (serverMonitor.isUnhealthy(server)) {
            logger.warn("Server {} is unhealthy with success-rate={}%", server, serverMonitor.getServerSuccessRate(server));
            return EchoServerResponse.builder()
                    .statusCode(HttpStatus.SERVICE_UNAVAILABLE)
                    .errorMessage(Optional.of("Service Unavailable"))
                    .upstreamServerName(server)
                    .build();
        }
        var endpoint = String.format("http://%s/echo", server);
        var requestEntity = buildRequest(request);
        try {
            var response = restTemplate.exchange(endpoint, HttpMethod.POST, requestEntity, String.class);
            return EchoServerResponse.builder()
                    .statusCode(response.getStatusCode())
                    .responseBody(response.getBody())
                    .errorMessage(Optional.empty())
                    .upstreamServerName(server)
                    .build();
        } catch (HttpClientErrorException ex) {
            logger.error("Error while executing {} to server {}", requestEntity, server, ex);
            return EchoServerResponse.builder()
                    .statusCode(ex.getStatusCode())
                    .responseBody(ex.getResponseBodyAsString())
                    .errorMessage(Optional.of(ex.getMessage()))
                    .upstreamServerName(server)
                    .build();
        } catch (Exception ex) {
            logger.error("Error while executing {} to server {}", requestEntity, server, ex);
            return EchoServerResponse.builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
                    .errorMessage(Optional.of(ex.getMessage()))
                    .upstreamServerName(server)
                    .build();
        }
    }

    private String getNextServer() {
        int nextIndex = (int) (requestCounter.getAndIncrement() % echoApiServerConfig.getServers().size());
        nextIndex = nextIndex < 0 ? nextIndex + echoApiServerConfig.getServers().size() : nextIndex;
        return echoApiServerConfig.getServers().get(nextIndex);
    }

}
