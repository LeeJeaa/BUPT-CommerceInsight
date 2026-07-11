package com.bupt.commerceinsight.importexport.vo;

public class ImportTaskVO {

    private final Long taskId;
    private final String tableName;
    private final String fileName;
    private final String status;
    private final Long totalRows;
    private final Long successRows;
    private final Long failedRows;
    private final Long elapsedMs;
    private final String startedAt;
    private final String endedAt;

    public ImportTaskVO(Long taskId, String tableName, String fileName, String status, Long totalRows,
                        Long successRows, Long failedRows, Long elapsedMs, String startedAt, String endedAt) {
        this.taskId = taskId;
        this.tableName = tableName;
        this.fileName = fileName;
        this.status = status;
        this.totalRows = totalRows;
        this.successRows = successRows;
        this.failedRows = failedRows;
        this.elapsedMs = elapsedMs;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }

    public Long getTaskId() {
        return taskId;
    }

    public String getTableName() {
        return tableName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getStatus() {
        return status;
    }

    public Long getTotalRows() {
        return totalRows;
    }

    public Long getSuccessRows() {
        return successRows;
    }

    public Long getFailedRows() {
        return failedRows;
    }

    public Long getElapsedMs() {
        return elapsedMs;
    }

    public String getStartedAt() {
        return startedAt;
    }

    public String getEndedAt() {
        return endedAt;
    }
}
