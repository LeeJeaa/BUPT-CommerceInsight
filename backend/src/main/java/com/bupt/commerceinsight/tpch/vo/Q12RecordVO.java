package com.bupt.commerceinsight.tpch.vo;

public class Q12RecordVO {

    private String shipMode;
    private Long highLineCount;
    private Long lowLineCount;

    public Q12RecordVO() {
    }

    public Q12RecordVO(String shipMode, Long highLineCount, Long lowLineCount) {
        this.shipMode = shipMode;
        this.highLineCount = highLineCount;
        this.lowLineCount = lowLineCount;
    }

    public String getShipMode() {
        return shipMode;
    }

    public Long getHighLineCount() {
        return highLineCount;
    }

    public Long getLowLineCount() {
        return lowLineCount;
    }

    public void setShipMode(String shipMode) {
        this.shipMode = shipMode;
    }

    public void setHighLineCount(Long highLineCount) {
        this.highLineCount = highLineCount;
    }

    public void setLowLineCount(Long lowLineCount) {
        this.lowLineCount = lowLineCount;
    }
}
