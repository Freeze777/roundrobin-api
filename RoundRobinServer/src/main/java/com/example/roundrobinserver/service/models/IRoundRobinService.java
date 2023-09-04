package com.example.roundrobinserver.service.models;

public interface IRoundRobinService {
    EchoServerResponse routeRequest(String message);
}
