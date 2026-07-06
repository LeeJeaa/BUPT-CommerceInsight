package com.bupt.commerceinsight.tpch.vo;

import java.math.BigDecimal;

public class Q14RecordVO {

    private BigDecimal promoRevenue;

    public Q14RecordVO() {
    }

    public Q14RecordVO(BigDecimal promoRevenue) {
        this.promoRevenue = promoRevenue;
    }

    public BigDecimal getPromoRevenue() {
        return promoRevenue;
    }

    public void setPromoRevenue(BigDecimal promoRevenue) {
        this.promoRevenue = promoRevenue;
    }
}
