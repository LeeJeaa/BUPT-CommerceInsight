package com.bupt.commerceinsight.auth;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mock")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginReturnsFrozenFields() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code", equalTo(200)))
            .andExpect(jsonPath("$.data.token", matchesPattern(
                "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"
            )))
            .andExpect(jsonPath("$.data.username", equalTo("admin")))
            .andExpect(jsonPath("$.data.role", equalTo("admin")))
            .andExpect(jsonPath("$.data.status", equalTo("approved")));
    }

    @Test
    void registerReturnsPendingStatus() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"bob\",\"password\":\"123456\",\"realName\":\"Bob\",\"email\":\"bob@example.com\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.username", equalTo("bob")))
            .andExpect(jsonPath("$.data.status", equalTo("pending")));
    }

    @Test
    void malformedJsonReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code", equalTo(400)));
    }
}
