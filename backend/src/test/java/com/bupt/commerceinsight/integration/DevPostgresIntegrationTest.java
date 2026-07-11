package com.bupt.commerceinsight.integration;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.mock.web.MockMultipartFile;
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

    private Long importedOrderKey;
    private Long lineItemOrderKey;
    private Integer importedLineNumber;

    @AfterEach
    void cleanUpTestRows() {
        if (lineItemOrderKey != null && importedLineNumber != null) {
            jdbcTemplate.update(
                "DELETE FROM lineitem WHERE l_orderkey = ? AND l_linenumber = ?",
                lineItemOrderKey,
                importedLineNumber
            );
        }
        if (importedOrderKey != null) {
            jdbcTemplate.update("DELETE FROM orders WHERE o_orderkey = ?", importedOrderKey);
        }
        jdbcTemplate.update("DELETE FROM performance_result WHERE test_name = 'Codex Integration Performance'");
        jdbcTemplate.update("""
            DELETE FROM import_error_log
            WHERE task_id IN (
                SELECT task_id FROM import_task
                WHERE file_name IN ('integration-orders.tbl', 'integration-lineitem.tbl')
            )
            """);
        jdbcTemplate.update("""
            DELETE FROM import_task
            WHERE file_name IN ('integration-orders.tbl', 'integration-lineitem.tbl')
            """);
    }

    @Test
    void devProfileRejectsMockTokenAndEnforcesUserRoles() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer mock-token"))
            .andExpect(status().isUnauthorized());

        String userToken = login("user1", "user123");
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isForbidden());

        String adminToken = login("admin", "admin123");
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk());
    }

    @Test
    void q5NewOrderAndPaymentWorkAgainstDevPostgres() throws Exception {
        String adminToken = login("admin", "admin123");

        mockMvc.perform(get("/api/tpch/q5?regionName=ASIA&startDate=1994-01-01&endDate=1995-01-01")
                .header("Authorization", "Bearer " + adminToken))
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
                .header("Authorization", "Bearer " + adminToken)
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
                .header("Authorization", "Bearer " + adminToken)
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

    @Test
    void importsReturnFinalCountsAndPersistRowErrors() throws Exception {
        String adminToken = login("admin", "admin123");
        long customerKey = jdbcTemplate.queryForObject(
            "SELECT c_custkey FROM customer ORDER BY c_custkey LIMIT 1", Long.class
        );
        importedOrderKey = jdbcTemplate.queryForObject(
            "SELECT COALESCE(MAX(o_orderkey), 0) + 100000 FROM orders", Long.class
        );
        long missingCustomerKey = jdbcTemplate.queryForObject(
            "SELECT COALESCE(MAX(c_custkey), 0) + 100000 FROM customer", Long.class
        );
        String orders = "%d|%d|O|100.00|1998-01-01|1-URGENT|Clerk#000000001|0|integration|%n"
            .formatted(importedOrderKey, customerKey)
            + "%d|%d|O|100.00|1998-01-01|1-URGENT|Clerk#000000001|0|duplicate|%n"
                .formatted(importedOrderKey, customerKey)
            + "%d|%d|O|100.00|1998-01-01|1-URGENT|Clerk#000000001|0|missing customer|%n"
                .formatted(importedOrderKey + 1, missingCustomerKey);

        MockMultipartFile ordersFile = new MockMultipartFile(
            "file", "integration-orders.tbl", "text/plain", orders.getBytes()
        );
        String orderResponse = mockMvc.perform(multipart("/api/import/tasks")
                .file(ordersFile)
                .param("tableName", "orders")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("success"))
            .andExpect(jsonPath("$.data.totalRows").value(3))
            .andExpect(jsonPath("$.data.successRows").value(1))
            .andExpect(jsonPath("$.data.failedRows").value(2))
            .andExpect(jsonPath("$.data.elapsedMs").isNumber())
            .andReturn().getResponse().getContentAsString();
        Number orderTaskId = JsonPath.read(orderResponse, "$.data.taskId");

        mockMvc.perform(get("/api/import/tasks/{taskId}/errors", orderTaskId.longValue())
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").value(2));

        Map<String, Object> lineContext = jdbcTemplate.queryForMap("""
            SELECT o.o_orderkey, ps.ps_partkey, ps.ps_suppkey,
                   COALESCE((
                       SELECT MAX(li.l_linenumber)
                       FROM lineitem li
                       WHERE li.l_orderkey = o.o_orderkey
                   ), 0) + 100 AS next_line
            FROM (SELECT o_orderkey FROM orders ORDER BY o_orderkey LIMIT 1) o
            CROSS JOIN (
                SELECT ps_partkey, ps_suppkey
                FROM partsupp
                ORDER BY ps_partkey, ps_suppkey
                LIMIT 1
            ) ps
            """);
        lineItemOrderKey = ((Number) lineContext.get("o_orderkey")).longValue();
        long partKey = ((Number) lineContext.get("ps_partkey")).longValue();
        long supplierKey = ((Number) lineContext.get("ps_suppkey")).longValue();
        importedLineNumber = ((Number) lineContext.get("next_line")).intValue();
        long missingPartKey = jdbcTemplate.queryForObject(
            "SELECT COALESCE(MAX(p_partkey), 0) + 100000 FROM part", Long.class
        );
        String lineItems = lineItemRow(lineItemOrderKey, partKey, supplierKey, importedLineNumber)
            + lineItemRow(lineItemOrderKey, partKey, supplierKey, importedLineNumber)
            + lineItemRow(lineItemOrderKey, missingPartKey, supplierKey, importedLineNumber + 1);
        MockMultipartFile lineItemFile = new MockMultipartFile(
            "file", "integration-lineitem.tbl", "text/plain", lineItems.getBytes()
        );

        String lineItemResponse = mockMvc.perform(multipart("/api/import/tasks")
                .file(lineItemFile)
                .param("tableName", "lineitem")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("success"))
            .andExpect(jsonPath("$.data.totalRows").value(3))
            .andExpect(jsonPath("$.data.successRows").value(1))
            .andExpect(jsonPath("$.data.failedRows").value(2))
            .andReturn().getResponse().getContentAsString();
        Number lineItemTaskId = JsonPath.read(lineItemResponse, "$.data.taskId");

        mockMvc.perform(get("/api/import/tasks/{taskId}/errors", lineItemTaskId.longValue())
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").value(2));
    }

    @Test
    void performanceResultCanBeWrittenAndReadImmediately() throws Exception {
        String adminToken = login("admin", "admin123");
        String request = """
            {
              "testName": "Codex Integration Performance",
              "testType": "tpch",
              "threadCount": 3,
              "totalRequests": 20,
              "successCount": 19,
              "failCount": 1,
              "avgLatencyMs": 12.5,
              "maxLatencyMs": 20.0,
              "minLatencyMs": 5.0,
              "throughput": 30.0
            }
            """;

        mockMvc.perform(post("/api/performance/results")
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .content(request))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.testName").value("Codex Integration Performance"));

        mockMvc.perform(get("/api/performance/results?testType=tpch")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.testName").value("Codex Integration Performance"))
            .andExpect(jsonPath("$.data.successCount").value(19));
    }

    private String login(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content("{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.data.token");
    }

    private String lineItemRow(long orderKey, long partKey, long supplierKey, int lineNumber) {
        return "%d|%d|%d|%d|1.00|10.00|0.10|0.10|N|O|1998-01-01|1998-01-01|1998-01-02|DELIVER IN PERSON|AIR|integration|%n"
            .formatted(orderKey, partKey, supplierKey, lineNumber);
    }
}
