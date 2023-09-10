package com.example.roundrobinserver.core.models;

import com.example.roundrobinserver.service.models.EchoServerResponse;

public interface IRequestExecutor {
    EchoServerResponse executeRequest(String request);
}
