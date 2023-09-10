package com.example.roundrobinserver.service;

import com.example.roundrobinserver.config.EchoApiConfig;
import com.example.roundrobinserver.core.RoundRobinRequestExecutor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RoundRobinRequestExecutorTest {

    private static Stream<Arguments> happyPathTestData() {
        return Stream.of(
                Arguments.of(3, 2, HttpStatus.OK, HttpStatus.OK), // all servers are healthy, no retries
                Arguments.of(5, 2, HttpStatus.OK, HttpStatus.OK), // all servers are healthy, more servers, no retries
                Arguments.of(3, 2, HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST), // all servers are healthy, request is bad, no retries
                Arguments.of(3, 1, HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR) // all servers are unhealthy, no retries by config
        );
    }

    @ParameterizedTest
    @MethodSource("happyPathTestData")
    public void testHappyPath(int numApiServers, int retries, HttpStatus mockServerStatus, HttpStatus expectedStatus) {
        var mockEchoApiConfig = getMockEchoApiConfig(numApiServers, retries);
        var mockResponseEntity = new ResponseEntity<>("MockResponse", mockServerStatus);
        var restTemplate = mock(RestTemplate.class);
        when(restTemplate.exchange(anyString(), Mockito.eq(HttpMethod.POST), any(), Mockito.eq(String.class), any(Object[].class)))
                .thenReturn(mockResponseEntity);

        for (int step = 0; step < 5; step++) {
            var roundRobinRequestExecutor = new RoundRobinRequestExecutor(mockEchoApiConfig, restTemplate);
            for (int i = 1; i <= numApiServers; i++) {
                var response = roundRobinRequestExecutor.executeRequest("MockRequest");
                assertEquals(response.getStatusCode(), expectedStatus);
                assertEquals(response.getResponseBody(), "MockResponse");
                assertEquals(response.getUpstreamServerName(), String.format("server%d", i));
            }
        }
    }

    @Test
    public void testAllServersUnhealthy() {
        int numApiServers = 3;
        var mockEchoApiConfig = getMockEchoApiConfig(numApiServers, 2);
        var mockResponseEntity = new ResponseEntity<>("MockResponse", HttpStatus.INTERNAL_SERVER_ERROR);
        var restTemplate = mock(RestTemplate.class);
        when(restTemplate.exchange(anyString(), Mockito.eq(HttpMethod.POST), any(), Mockito.eq(String.class), any(Object[].class)))
                .thenReturn(mockResponseEntity);

        var roundRobinRequestExecutor = new RoundRobinRequestExecutor(mockEchoApiConfig, restTemplate);
        for (int i = 1; i <= 15; i++) {
            assertTrue(roundRobinRequestExecutor.executeRequest("MockRequest").getStatusCode().is5xxServerError());
        }

        // all servers are unhealthy
        for (int i = 0; i < numApiServers; i++) {
            assertEquals(roundRobinRequestExecutor.executeRequest("MockRequest").getStatusCode(), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @Test
    public void testRetry() {
        int numApiServers = 3;
        var mockEchoApiConfig = getMockEchoApiConfig(numApiServers, numApiServers);
        var mockResponseEntity = new ResponseEntity<>("MockResponse", HttpStatus.INTERNAL_SERVER_ERROR);
        var restTemplate = mock(RestTemplate.class);
        when(restTemplate.exchange(anyString(), Mockito.eq(HttpMethod.POST), any(), Mockito.eq(String.class), any(Object[].class)))
                .thenReturn(mockResponseEntity);

        var roundRobinRequestExecutor = new RoundRobinRequestExecutor(mockEchoApiConfig, restTemplate);
        var response = roundRobinRequestExecutor.executeRequest("MockRequest");
        assertTrue(response.getStatusCode().is5xxServerError());
        assertEquals(response.getUpstreamServerName(), "server3");
        assertEquals(response.getResponseBody(), "MockResponse");
    }

    @Test
    public void testHttpClientErrorException() {
        var mockEchoApiConfig = getMockEchoApiConfig(3, 1);
        var restTemplate = mock(RestTemplate.class);
        when(restTemplate.exchange(anyString(), Mockito.eq(HttpMethod.POST), any(), Mockito.eq(String.class), any(Object[].class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "MockError"));

        var roundRobinRequestExecutor = new RoundRobinRequestExecutor(mockEchoApiConfig, restTemplate);
        var response = roundRobinRequestExecutor.executeRequest("MockRequest");
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(response.getUpstreamServerName(), "server1");
        assertEquals(response.getErrorMessage().get(), "400 MockError");
    }

    @Test
    public void testIOErrors() {
        var mockEchoApiConfig = getMockEchoApiConfig(3, 1);
        var restTemplate = mock(RestTemplate.class);
        when(restTemplate.exchange(anyString(), Mockito.eq(HttpMethod.POST), any(), Mockito.eq(String.class), any(Object[].class)))
                .thenThrow(new ResourceAccessException("MockError"));

        var roundRobinRequestExecutor = new RoundRobinRequestExecutor(mockEchoApiConfig, restTemplate);
        var response = roundRobinRequestExecutor.executeRequest("MockRequest");
        assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
        assertEquals(response.getUpstreamServerName(), "server1");
        assertEquals(response.getErrorMessage().get(), "MockError");

    }


    private static EchoApiConfig getMockEchoApiConfig(int numApiServers, int retries) {
        var echoApiConfig = mock(EchoApiConfig.class);
        var serverList = new ArrayList<String>();
        for (int i = 1; i <= numApiServers; i++) serverList.add(String.format("server%d", i));
        when(echoApiConfig.getServers()).thenReturn(serverList);
        when(echoApiConfig.getRetries()).thenReturn(retries);
        when(echoApiConfig.getBackoffTimeMs()).thenReturn(1);
        when(echoApiConfig.getBackoffMultiplier()).thenReturn(2);
        when(echoApiConfig.getMinSuccessRate()).thenReturn(0.1);
        when(echoApiConfig.getServers()).thenReturn(serverList);
        return echoApiConfig;
    }
}
