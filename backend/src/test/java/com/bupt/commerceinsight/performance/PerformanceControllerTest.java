package com.bupt.commerceinsight.performance;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
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
class PerformanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void performanceResultsUseApiContractFields() throws Exception {
        mockMvc.perform(get("/api/performance/results?testType=tpch")
                .header("Authorization", "Bearer mock-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.successRequests", equalTo(80)))
            .andExpect(jsonPath("$.data.failedRequests", equalTo(0)))
            .andExpect(jsonPath("$.data.throughputQps", equalTo(6.35)))
            .andExpect(jsonPath("$.data.records[0].throughputQps", equalTo(2.43)))
            .andExpect(jsonPath("$.data.chartData", hasKey("latencySeries")))
            .andExpect(jsonPath("$.data.chartData", hasKey("throughputSeries")));
    }
}
