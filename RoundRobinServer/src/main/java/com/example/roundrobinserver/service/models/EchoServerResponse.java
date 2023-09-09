package com.example.roundrobinserver.service.models;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatusCode;

import java.util.Optional;

@Getter
@Builder
public class EchoServerResponse {
    private HttpStatusCode statusCode;
    private String responseBody;
    private String upstreamServerName;
    private Optional<String> errorMessage;
}
