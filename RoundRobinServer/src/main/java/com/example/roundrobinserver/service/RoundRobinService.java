package com.example.roundrobinserver.service;


import com.example.roundrobinserver.service.models.EchoServerResponse;
import com.example.roundrobinserver.core.models.IRequestExecutor;
import com.example.roundrobinserver.service.models.IRoundRobinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoundRobinService implements IRoundRobinService {

    private final IRequestExecutor requestExecutor;

    @Autowired
    public RoundRobinService(IRequestExecutor requestExecutor) {
        this.requestExecutor = requestExecutor;
    }


    @Override
    public EchoServerResponse routeRequest(String message) {
        return requestExecutor.executeRequest(message);
    }
}
