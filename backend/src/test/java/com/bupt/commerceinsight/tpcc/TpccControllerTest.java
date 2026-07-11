package com.bupt.commerceinsight.tpcc;

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
class TpccControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void newOrderReturnsFrozenFields() throws Exception {
        mockMvc.perform(post("/api/tpcc/new-order")
                .header("Authorization", "Bearer mock-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"warehouseId\":1,\"districtId\":1,\"customerId\":1,\"items\":[{\"itemId\":1001,\"quantity\":5}]}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.transactionId", matchesPattern("NO-[0-9]{8}-[0-9]{4}")))
            .andExpect(jsonPath("$.data.status", equalTo("committed")))
            .andExpect(jsonPath("$.data.orderId", equalTo(3001)))
            .andExpect(jsonPath("$.data.totalAmount", equalTo(125.50)));
    }

    @Test
    void paymentAcceptsPaymentAmountAndReturnsNewBalance() throws Exception {
        mockMvc.perform(post("/api/tpcc/payment")
                .header("Authorization", "Bearer mock-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"warehouseId\":1,\"districtId\":1,\"customerId\":1,\"paymentAmount\":100.00}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.transactionId", matchesPattern("PAY-[0-9]{8}-[0-9]{4}")))
            .andExpect(jsonPath("$.data.status", equalTo("committed")))
            .andExpect(jsonPath("$.data.customerId", equalTo(1)))
            .andExpect(jsonPath("$.data.newBalance", equalTo(520.25)));
    }
}
