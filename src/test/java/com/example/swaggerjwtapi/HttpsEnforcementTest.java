package com.example.swaggerjwtapi;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class HttpsEnforcementTest {

    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    @ActiveProfiles("prod-test")
    class WithHttpsRequired {

        @Autowired
        private MockMvc mockMvc;

        @Test
        public void httpRedirectsToHttps() throws Exception {
            mockMvc.perform(post("/api/auth/login"))
                    .andExpect(status().isFound())
                    .andExpect(header().exists("Location"))
                    .andExpect(header().string("Location", containsString("https://")));
        }

        @Test
        public void hstsHeaderIsPresentOnSecureRequest() throws Exception {
            mockMvc.perform(post("/api/auth/login").secure(true))
                    .andExpect(header().string("Strict-Transport-Security",
                            containsString("max-age=31536000")));
        }
    }

    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    @ActiveProfiles("test")
    class WithHttpsNotRequired {

        @Autowired
        private MockMvc mockMvc;

        @Test
        public void httpDoesNotRedirect() throws Exception {
            mockMvc.perform(post("/api/auth/login"))
                    .andExpect(status().isBadRequest());
        }
    }
}
