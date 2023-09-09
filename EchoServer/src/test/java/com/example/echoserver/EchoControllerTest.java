package com.example.echoserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EchoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testEchoController() throws Exception {
        var body = """
                {
                    "message": "Hello, World!"
                }
               """;
        mockMvc.perform(post("/echo").content(body))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(body)));
    }

}
