package com.example.roundrobinserver.service.utils;

import org.springframework.http.*;

public class HttpUtils {
    public static HttpEntity<String> buildRequest(String requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(requestBody, headers);
    }

    public static boolean isRetryableError(HttpStatusCode statusCode) {
        return statusCode.is5xxServerError() || statusCode.equals(HttpStatus.TOO_MANY_REQUESTS);
    }

    public static boolean isSuccessful(HttpStatusCode statusCode) {
        return statusCode.is2xxSuccessful();
    }
}
