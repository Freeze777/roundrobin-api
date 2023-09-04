package com.example.roundrobinserver.service.models;

public interface IRequestExecutor {
    EchoServerResponse executeRequest(String message);
}
