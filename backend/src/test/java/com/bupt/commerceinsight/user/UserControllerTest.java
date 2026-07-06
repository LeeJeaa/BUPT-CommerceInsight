package com.bupt.commerceinsight.user;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mock")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void usersRequireAdminToken() throws Exception {
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code", equalTo(401)));
    }

    @Test
    void listUsersReturnsPagedRecords() throws Exception {
        mockMvc.perform(get("/api/users?pageNo=1&pageSize=20")
                .header("Authorization", "Bearer mock-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.pageNo", equalTo(1)))
            .andExpect(jsonPath("$.data.records[0].userId", equalTo(1)))
            .andExpect(jsonPath("$.data.records[0].realName", equalTo("管理员")));
    }

    @Test
    void approveUserReturnsStatusOnly() throws Exception {
        mockMvc.perform(put("/api/users/3/approve")
                .header("Authorization", "Bearer mock-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.userId", equalTo(3)))
            .andExpect(jsonPath("$.data.status", equalTo("approved")));
    }
}
