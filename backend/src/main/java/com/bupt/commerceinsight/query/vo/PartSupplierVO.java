package com.bupt.commerceinsight.query.vo;

import java.math.BigDecimal;

public class PartSupplierVO {

    private final Long partKey;
    private final String partName;
    private final String supplierName;
    private final String nationName;
    private final Integer availQty;
    private final BigDecimal supplyCost;

    public PartSupplierVO(
        Long partKey,
        String partName,
        String supplierName,
        String nationName,
        Integer availQty,
        BigDecimal supplyCost
    ) {
        this.partKey = partKey;
        this.partName = partName;
        this.supplierName = supplierName;
        this.nationName = nationName;
        this.availQty = availQty;
        this.supplyCost = supplyCost;
    }

    public Long getPartKey() {
        return partKey;
    }

    public String getPartName() {
        return partName;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public String getNationName() {
        return nationName;
    }

    public Integer getAvailQty() {
        return availQty;
    }

    public BigDecimal getSupplyCost() {
        return supplyCost;
    }
}
