package com.example.roundrobinserver.service;

import com.example.roundrobinserver.config.EchoApiConfig;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RoundRobinRequestExecutorTest {

    private static Stream<Arguments> happyPathTestData() {
        return Stream.of(
                Arguments.of(3, HttpStatus.OK, HttpStatus.OK), // all servers are healthy
                Arguments.of(5, HttpStatus.OK, HttpStatus.OK), // all servers are healthy, more servers
                Arguments.of(3, HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST), // all servers are healthy, request is bad
                Arguments.of(3, HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR) // all servers are unhealthy, no retries
        );
    }

    @ParameterizedTest
    @MethodSource("happyPathTestData")
    public void testExecuteRequestHappyPath(int numApiServers, HttpStatus mockServerStatus, HttpStatus expectedStatus) {
        var echoApiConfig = mock(EchoApiConfig.class);
        var serverList = new ArrayList<String>();
        for (int i = 1; i <= numApiServers; i++) serverList.add(String.format("server%d", i));
        when(echoApiConfig.getServers()).thenReturn(serverList);
        when(echoApiConfig.getRetries()).thenReturn(1);
        when(echoApiConfig.getBackoffTimeMs()).thenReturn(1);
        when(echoApiConfig.getMinSuccessRate()).thenReturn(0.1);
        when(echoApiConfig.getServers()).thenReturn(serverList);

        ResponseEntity<String> mockResponseEntity = new ResponseEntity<>("MockResponse", mockServerStatus);
        var restTemplate = mock(RestTemplate.class);
        when(restTemplate.exchange(anyString(), Mockito.eq(HttpMethod.POST), any(), Mockito.eq(String.class), any(Object[].class)))
                .thenReturn(mockResponseEntity);

        var roundRobinRequestExecutor = new RoundRobinRequestExecutor(echoApiConfig, restTemplate);

        for (int step = 0; step < 5; step++) {
            for (int i = 1; i <= numApiServers; i++) {
                var response = roundRobinRequestExecutor.executeRequest("MockRequest");
                assertEquals(response.getStatusCode(), expectedStatus);
                assertEquals(response.getResponseBody(), "MockResponse");
                assertEquals(response.getUpstreamServerName(), String.format("server%d", i));
            }
        }
    }
}
