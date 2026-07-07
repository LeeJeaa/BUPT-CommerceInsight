package com.bupt.commerceinsight.query;

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
class BusinessQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void customersReturnApiContractFields() throws Exception {
        mockMvc.perform(get("/api/query/customers?nationName=CHINA")
                .header("Authorization", "Bearer mock-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.records[0].customerKey", equalTo(1)))
            .andExpect(jsonPath("$.data.records[0].customerName", equalTo("Customer#000000001")))
            .andExpect(jsonPath("$.data.records[0].nationName", equalTo("CHINA")));
    }

    @Test
    void orderRevenueReturnsApiContractFields() throws Exception {
        mockMvc.perform(get("/api/query/order-revenue?startDate=1996-01-01&endDate=1996-12-31")
                .header("Authorization", "Bearer mock-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.records[0].orderKey", equalTo(1)))
            .andExpect(jsonPath("$.data.records[0].orderDate", equalTo("1996-01-02")))
            .andExpect(jsonPath("$.data.records[0].revenue", equalTo(172799.49)));
    }

    @Test
    void partSupplierReturnsApiContractFields() throws Exception {
        mockMvc.perform(get("/api/query/part-supplier?keyword=Supplier")
                .header("Authorization", "Bearer mock-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.records[0].partKey", equalTo(1001)))
            .andExpect(jsonPath("$.data.records[0].partName", equalTo("Part-1001")))
            .andExpect(jsonPath("$.data.records[0].supplierName", equalTo("Supplier#000000001")))
            .andExpect(jsonPath("$.data.records[0].nationName", equalTo("CHINA")));
    }
}
