package com.bupt.commerceinsight.importexport.vo;

public class ImportErrorVO {

    private final Long lineNumber;
    private final String fieldName;
    private final String fieldValue;
    private final String errorReason;

    public ImportErrorVO(Long lineNumber, String fieldName, String fieldValue, String errorReason) {
        this.lineNumber = lineNumber;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.errorReason = errorReason;
    }

    public Long getLineNumber() {
        return lineNumber;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public String getErrorReason() {
        return errorReason;
    }
}
