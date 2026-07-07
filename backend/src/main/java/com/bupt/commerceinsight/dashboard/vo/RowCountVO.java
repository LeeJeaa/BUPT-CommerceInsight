package com.bupt.commerceinsight.dashboard.vo;

public class RowCountVO {

    private final String tableName;
    private final Long rowCount;

    public RowCountVO(String tableName, Long rowCount) {
        this.tableName = tableName;
        this.rowCount = rowCount;
    }

    public String getTableName() {
        return tableName;
    }

    public Long getRowCount() {
        return rowCount;
    }
}
