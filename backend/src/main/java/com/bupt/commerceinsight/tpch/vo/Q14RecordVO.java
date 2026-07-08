package com.bupt.commerceinsight.tpch.vo;

import java.math.BigDecimal;

public class Q14RecordVO {

    private BigDecimal promoRevenuePercent;

    public Q14RecordVO() {
    }

    public Q14RecordVO(BigDecimal promoRevenuePercent) {
        this.promoRevenuePercent = promoRevenuePercent;
    }

    public BigDecimal getPromoRevenuePercent() {
        return promoRevenuePercent;
    }

    public void setPromoRevenuePercent(BigDecimal promoRevenuePercent) {
        this.promoRevenuePercent = promoRevenuePercent;
    }
}
