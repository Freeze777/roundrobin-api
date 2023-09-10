package com.example.roundrobinserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RoundRobinServerApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testEchoControllerSuccess() throws Exception {
        var body = """
                 {
                     "message": "Hello, World!",
                      "name": "John Doe"
                 }
                """;
        mockMvc.perform(post("/api/echo").content(body))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(body)));
    }


    @Test
    public void testEchoControllerBadRequest() throws Exception {
        var body = """
                 {
                    "message": "Im not a valid json
                 }
                """;
        mockMvc.perform(post("/api/echo").content(body))
                .andExpect(status().isBadRequest());
    }


    @Test
    public void lowLoadConcurrentRequests() throws Exception {
        var threads = new ArrayList<Thread>();
        for (int i = 0; i < 15; i++) {
            var body = "{ \"message\": \"Hello," + i + " World!\" }";
            threads.add(new Thread(() -> {
                try {
                    mockMvc.perform(post("/api/echo").content(body))
                            .andExpect(status().isOk())
                            .andExpect(content().string(equalTo(body)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
        }
        threads.forEach(Thread::start);
        for (var thread : threads) thread.join();
    }

    @Test
    public void highLoadConcurrentRequests() throws Exception {
        var threads = new ArrayList<Thread>();
        for (int i = 0; i < 500; i++) {
            var body = "{ \"message\": \"Hello," + i + " World!\" }";
            threads.add(new Thread(() -> {
                try {
                    mockMvc.perform(post("/api/echo").content(body))
                            .andExpect(status().isOk())
                            .andExpect(content().string(equalTo(body)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
        }
        threads.forEach(Thread::start);
        for (var thread : threads) thread.join();
    }

}
