package com.example.swaggerjwtapi;

import com.example.swaggerjwtapi.dto.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testLoggingSuccess() throws Exception {
        LoginRequest request =
                new LoginRequest("admin", "12345");

        MvcResult result =
                mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                        )
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.accessToken").exists())
                        .andExpect(jsonPath("$.refreshToken").exists())
                        .andReturn();

        System.out.println("login OK");
    }

    @Test
    public void testInvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("admin", "54321");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
