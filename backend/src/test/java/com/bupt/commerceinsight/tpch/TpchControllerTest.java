package com.bupt.commerceinsight.tpch;

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
class TpchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void q5ReturnsRecordsAndChartData() throws Exception {
        mockMvc.perform(get("/api/tpch/q5?regionName=ASIA&startDate=1994-01-01&endDate=1995-01-01")
                .header("Authorization", "Bearer mock-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.queryName", equalTo("TPC-H Q5 本地供应商收入分析")))
            .andExpect(jsonPath("$.data.records[0].nationName", equalTo("CHINA")))
            .andExpect(jsonPath("$.data.chartData", hasKey("xAxis")))
            .andExpect(jsonPath("$.data.chartData", hasKey("series")));
    }

    @Test
    void q12UsesMockContractChartFields() throws Exception {
        mockMvc.perform(get("/api/tpch/q12?shipMode1=MAIL&shipMode2=SHIP&startDate=1994-01-01&endDate=1995-01-01")
                .header("Authorization", "Bearer mock-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.records[0].shipMode", equalTo("MAIL")))
            .andExpect(jsonPath("$.data.chartData", hasKey("highPrioritySeries")))
            .andExpect(jsonPath("$.data.chartData", hasKey("lowPrioritySeries")));
    }

    @Test
    void q1ReturnsApiContractRecordFields() throws Exception {
        mockMvc.perform(get("/api/tpch/q1?shipDate=1998-09-01")
                .header("Authorization", "Bearer mock-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.records[0].returnFlag", equalTo("A")))
            .andExpect(jsonPath("$.data.records[0].lineStatus", equalTo("F")))
            .andExpect(jsonPath("$.data.records[0].sumCharge", equalTo(5.590906522283E10)))
            .andExpect(jsonPath("$.data.records[0].avgPrice", equalTo(38273.13)))
            .andExpect(jsonPath("$.data.records[0].avgDisc", equalTo(0.05)))
            .andExpect(jsonPath("$.data.records[0].countOrder", equalTo(1478493)));
    }

    @Test
    void q14ReturnsFrozenPromoRevenueField() throws Exception {
        mockMvc.perform(get("/api/tpch/q14?month=1995-09-01")
                .header("Authorization", "Bearer mock-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.records[0].promoRevenuePercent", equalTo(16.38)));
    }
}
