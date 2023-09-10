package com.example.roundrobinserver.core;

import com.example.roundrobinserver.config.RetryConfig;
import com.example.roundrobinserver.core.models.IServerMonitorStrategy;
import com.example.roundrobinserver.service.models.EchoServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

import static com.example.roundrobinserver.utils.HttpUtils.isRetryableError;
import static com.example.roundrobinserver.utils.HttpUtils.isSuccessful;

public class RetryWrapper {
    private static final Logger logger = LoggerFactory.getLogger(RetryWrapper.class);
    RetryConfig retryConfig;
    IServerMonitorStrategy serverMonitor;

    public RetryWrapper(RetryConfig retryConfig, IServerMonitorStrategy serverMonitor) {
        this.retryConfig = retryConfig;
        this.serverMonitor = serverMonitor;
    }

    public EchoServerResponse execute(String request, Function<String, EchoServerResponse> executorFunction) {
        var retries = retryConfig.getRetries();
        var backoffTimeMs = retryConfig.getBackoffTimeMs();
        EchoServerResponse response = null;
        while (retries > 0) {
            response = executorFunction.apply(request);
            if (isSuccessful(response.getStatusCode()) || !isRetryableError(response.getStatusCode())) {
                logger.info("Request to server {} was completed with {} status", response.getUpstreamServerName(), response.getStatusCode());
                serverMonitor.updateServerStats(response.getUpstreamServerName(), true);
                return response;
            }
            retries--;
            backoffTimeMs *= retryConfig.getBackoffMultiplier();
            serverMonitor.updateServerStats(response.getUpstreamServerName(), false);
            logger.error("Request to server {} with success-rate={}% failed for {}. Retrying in {} ms", response.getUpstreamServerName(), serverMonitor.getServerSuccessRate(response.getUpstreamServerName()), request, backoffTimeMs);
            try {
                Thread.sleep(backoffTimeMs);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return response;
    }
}
