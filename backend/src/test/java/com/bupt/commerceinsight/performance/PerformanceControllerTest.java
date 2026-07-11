package com.bupt.commerceinsight.performance;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
            .andExpect(jsonPath("$.data.successCount", equalTo(80)))
            .andExpect(jsonPath("$.data.failCount", equalTo(0)))
            .andExpect(jsonPath("$.data.throughput", equalTo(6.35)))
            .andExpect(jsonPath("$.data.records[0].throughput", equalTo(2.43)))
            .andExpect(jsonPath("$.data.chartData", hasKey("latencySeries")))
            .andExpect(jsonPath("$.data.chartData", hasKey("throughputSeries")));
    }

    @Test
    void adminCanWriteAndImmediatelyReadPerformanceResult() throws Exception {
        String body = """
            {
              "testName": "TPC-C Smoke",
              "testType": "tpcc",
              "threadCount": 4,
              "totalRequests": 100,
              "successCount": 98,
              "failCount": 2,
              "avgLatencyMs": 12.5,
              "maxLatencyMs": 30.0,
              "minLatencyMs": 5.0,
              "throughput": 80.0
            }
            """;

        mockMvc.perform(post("/api/performance/results")
                .header("Authorization", "Bearer mock-token")
                .contentType("application/json")
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.testName", equalTo("TPC-C Smoke")))
            .andExpect(jsonPath("$.data.failCount", equalTo(2)));

        mockMvc.perform(get("/api/performance/results?testType=tpcc")
                .header("Authorization", "Bearer mock-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.testName", equalTo("TPC-C Smoke")))
            .andExpect(jsonPath("$.data.throughput", equalTo(80.0)));
    }

    @Test
    void nonAdminCannotWritePerformanceResult() throws Exception {
        mockMvc.perform(post("/api/performance/results")
                .header("Authorization", "Bearer mock-token-user")
                .contentType("application/json")
                .content(validRequest()))
            .andExpect(status().isForbidden());
    }

    @Test
    void invalidFieldTypeReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/performance/results")
                .header("Authorization", "Bearer mock-token")
                .contentType("application/json")
                .content(validRequest().replace("\"threadCount\": 2", "\"threadCount\": \"many\"")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code", equalTo(400)));
    }

    @Test
    void inconsistentRequestCountsReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/performance/results")
                .header("Authorization", "Bearer mock-token")
                .contentType("application/json")
                .content(validRequest().replace("\"failCount\": 0", "\"failCount\": 1")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code", equalTo(400)));
    }

    private String validRequest() {
        return """
            {
              "testName": "TPC-H Smoke",
              "testType": "tpch",
              "threadCount": 2,
              "totalRequests": 10,
              "successCount": 10,
              "failCount": 0,
              "avgLatencyMs": 10.0,
              "maxLatencyMs": 20.0,
              "minLatencyMs": 5.0,
              "throughput": 50.0
            }
            """;
    }
}
