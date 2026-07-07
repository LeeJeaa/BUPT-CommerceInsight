package com.bupt.commerceinsight.dashboard;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void summaryReturnsDashboardContractFields() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary")
                .header("Authorization", "Bearer mock-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.databaseName", equalTo("tpc_commerce")))
            .andExpect(jsonPath("$.data.rowCounts[0].tableName", equalTo("region")))
            .andExpect(jsonPath("$.data.modules[0].status", equalTo("ready")));
    }
}
