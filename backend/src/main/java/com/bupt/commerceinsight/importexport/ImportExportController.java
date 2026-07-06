package com.bupt.commerceinsight.importexport;

import com.bupt.commerceinsight.common.ApiResponse;
import com.bupt.commerceinsight.common.PageResponse;
import com.bupt.commerceinsight.importexport.vo.ImportErrorVO;
import com.bupt.commerceinsight.importexport.vo.ImportTaskVO;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class ImportExportController {

    private final ImportExportService importExportService;

    public ImportExportController(ImportExportService importExportService) {
        this.importExportService = importExportService;
    }

    @PostMapping(value = "/import/tasks", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ImportTaskVO> createTask(
        @RequestParam String tableName,
        @RequestParam(required = false) MultipartFile file
    ) {
        return ApiResponse.success(importExportService.createImportTask(tableName, file));
    }

    @GetMapping("/import/tasks/{taskId}")
    public ApiResponse<ImportTaskVO> getTask(@PathVariable Long taskId) {
        return ApiResponse.success(importExportService.getTask(taskId));
    }

    @GetMapping("/import/tasks/{taskId}/errors")
    public ApiResponse<PageResponse<ImportErrorVO>> getErrors(
        @PathVariable Long taskId,
        @RequestParam(defaultValue = "1") int pageNo,
        @RequestParam(defaultValue = "20") int pageSize
    ) {
        return ApiResponse.success(importExportService.getErrors(taskId, pageNo, pageSize));
    }

    @GetMapping("/export/table/{tableName}")
    public ResponseEntity<ByteArrayResource> exportTable(@PathVariable String tableName) {
        ByteArrayResource resource = importExportService.exportTable(tableName);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + tableName + ".csv\"")
            .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
            .contentLength(resource.contentLength())
            .body(resource);
    }
}
