package com.bupt.commerceinsight.importexport.vo;

public class ImportErrorVO {

    private final Long lineNo;
    private final String fieldName;
    private final String rawValue;
    private final String reason;

    public ImportErrorVO(Long lineNo, String fieldName, String rawValue, String reason) {
        this.lineNo = lineNo;
        this.fieldName = fieldName;
        this.rawValue = rawValue;
        this.reason = reason;
    }

    public Long getLineNo() {
        return lineNo;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getRawValue() {
        return rawValue;
    }

    public String getReason() {
        return reason;
    }
}
