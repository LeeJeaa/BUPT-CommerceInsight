package com.bupt.commerceinsight.importexport;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mock")
class ImportExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createImportTaskReturnsFrozenFields() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", "orders_sample.tbl", "text/plain", "1|sample|".getBytes()
        );
        mockMvc.perform(multipart("/api/import/tasks")
                .file(file)
                .param("tableName", "orders")
                .header("Authorization", "Bearer mock-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.taskId", equalTo(1001)))
            .andExpect(jsonPath("$.data.tableName", equalTo("orders")))
            .andExpect(jsonPath("$.data.status", equalTo("success")))
            .andExpect(jsonPath("$.data.totalRows", equalTo(1)))
            .andExpect(jsonPath("$.data.successRows", equalTo(1)))
            .andExpect(jsonPath("$.data.failedRows", equalTo(0)))
            .andExpect(jsonPath("$.data.elapsedMs", equalTo(10)));
    }

    @Test
    void importRejectsUnsupportedTable() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", "partsupp.tbl", "text/plain", "1|sample|".getBytes()
        );
        mockMvc.perform(multipart("/api/import/tasks")
                .file(file)
                .param("tableName", "partsupp")
                .header("Authorization", "Bearer mock-token"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code", equalTo(400)));
    }

    @Test
    void importErrorsUseApiContractFieldNames() throws Exception {
        mockMvc.perform(get("/api/import/tasks/1001/errors")
                .header("Authorization", "Bearer mock-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.records[0].lineNumber", equalTo(18)))
            .andExpect(jsonPath("$.data.records[0].fieldValue", equalTo("-1")))
            .andExpect(jsonPath("$.data.records[0].errorReason", equalTo("金额不能为负数")));
    }

    @Test
    void exportTableReturnsFileStream() throws Exception {
        mockMvc.perform(get("/api/export/table/orders")
                .header("Authorization", "Bearer mock-token"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", "attachment; filename=\"orders.csv\""));
    }

    @Test
    void importAndExportRequireAdminRole() throws Exception {
        mockMvc.perform(get("/api/export/table/orders")
                .header("Authorization", "Bearer mock-token-user"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code", equalTo(403)));
    }
}
