package com.bupt.commerceinsight.tpch.vo;

import java.math.BigDecimal;

public class Q5RecordVO {

    private String nationName;
    private BigDecimal revenue;

    public Q5RecordVO() {
    }

    public Q5RecordVO(String nationName, BigDecimal revenue) {
        this.nationName = nationName;
        this.revenue = revenue;
    }

    public String getNationName() {
        return nationName;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setNationName(String nationName) {
        this.nationName = nationName;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }
}
