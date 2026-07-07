package com.bupt.commerceinsight.integration;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@EnabledIfEnvironmentVariable(named = "RUN_DEV_INTEGRATION_TESTS", matches = "true")
class DevPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void q5NewOrderAndPaymentWorkAgainstDevPostgres() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.role").value("admin"));

        mockMvc.perform(get("/api/tpch/q5?regionName=ASIA&startDate=1994-01-01&endDate=1995-01-01")
                .header("Authorization", "Bearer mock-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.queryName").exists())
            .andExpect(jsonPath("$.data.rowCount", greaterThanOrEqualTo(0)));

        Map<String, Object> context = jdbcTemplate.queryForMap("""
            SELECT w.w_id, d.d_id, c.c_id, i.i_id
            FROM warehouse w
            JOIN district d ON d.d_w_id = w.w_id
            JOIN tpcc_customer c ON c.c_w_id = d.d_w_id AND c.c_d_id = d.d_id
            JOIN stock s ON s.s_w_id = w.w_id AND s.s_quantity >= 2
            JOIN item i ON i.i_id = s.s_i_id
            ORDER BY w.w_id, d.d_id, c.c_id, i.i_id
            LIMIT 1
            """);

        String warehouseId = context.get("w_id").toString();
        String districtId = context.get("d_id").toString();
        String customerId = context.get("c_id").toString();
        String itemId = context.get("i_id").toString();

        mockMvc.perform(post("/api/tpcc/new-order")
                .header("Authorization", "Bearer mock-token")
                .contentType("application/json")
                .content("""
                    {
                      "warehouseId": %s,
                      "districtId": %s,
                      "customerId": %s,
                      "items": [{"itemId": %s, "quantity": 1}]
                    }
                    """.formatted(warehouseId, districtId, customerId, itemId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("committed"));

        mockMvc.perform(post("/api/tpcc/payment")
                .header("Authorization", "Bearer mock-token")
                .contentType("application/json")
                .content("""
                    {
                      "warehouseId": %s,
                      "districtId": %s,
                      "customerId": %s,
                      "paymentAmount": 10.00
                    }
                    """.formatted(warehouseId, districtId, customerId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("committed"));
    }
}
