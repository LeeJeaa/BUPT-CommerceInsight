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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mock")
class ImportExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createImportTaskReturnsFrozenFields() throws Exception {
        mockMvc.perform(multipart("/api/import/tasks")
                .param("tableName", "orders")
                .header("Authorization", "Bearer mock-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.taskId", equalTo(1001)))
            .andExpect(jsonPath("$.data.tableName", equalTo("orders")))
            .andExpect(jsonPath("$.data.status", equalTo("running")));
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
}
