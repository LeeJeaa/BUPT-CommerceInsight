package com.bupt.commerceinsight.tpch.vo;

import java.math.BigDecimal;

public class Q1RecordVO {

    private String returnFlag;
    private String lineStatus;
    private BigDecimal sumQuantity;
    private BigDecimal sumBasePrice;
    private BigDecimal sumDiscountedPrice;
    private BigDecimal sumCharge;
    private BigDecimal avgQuantity;
    private BigDecimal avgPrice;
    private BigDecimal avgDisc;
    private Long countOrder;

    public Q1RecordVO() {
    }

    public Q1RecordVO(String returnFlag, String lineStatus, BigDecimal sumQuantity, BigDecimal sumBasePrice,
                      BigDecimal sumDiscountedPrice, BigDecimal sumCharge, BigDecimal avgQuantity,
                      BigDecimal avgPrice, BigDecimal avgDisc, Long countOrder) {
        this.returnFlag = returnFlag;
        this.lineStatus = lineStatus;
        this.sumQuantity = sumQuantity;
        this.sumBasePrice = sumBasePrice;
        this.sumDiscountedPrice = sumDiscountedPrice;
        this.sumCharge = sumCharge;
        this.avgQuantity = avgQuantity;
        this.avgPrice = avgPrice;
        this.avgDisc = avgDisc;
        this.countOrder = countOrder;
    }

    public String getReturnFlag() {
        return returnFlag;
    }

    public String getLineStatus() {
        return lineStatus;
    }

    public BigDecimal getSumQuantity() {
        return sumQuantity;
    }

    public BigDecimal getSumBasePrice() {
        return sumBasePrice;
    }

    public BigDecimal getSumDiscountedPrice() {
        return sumDiscountedPrice;
    }

    public BigDecimal getSumCharge() {
        return sumCharge;
    }

    public BigDecimal getAvgQuantity() {
        return avgQuantity;
    }

    public BigDecimal getAvgPrice() {
        return avgPrice;
    }

    public BigDecimal getAvgDisc() {
        return avgDisc;
    }

    public Long getCountOrder() {
        return countOrder;
    }

    public void setReturnFlag(String returnFlag) {
        this.returnFlag = returnFlag;
    }

    public void setLineStatus(String lineStatus) {
        this.lineStatus = lineStatus;
    }

    public void setSumQuantity(BigDecimal sumQuantity) {
        this.sumQuantity = sumQuantity;
    }

    public void setSumBasePrice(BigDecimal sumBasePrice) {
        this.sumBasePrice = sumBasePrice;
    }

    public void setSumDiscountedPrice(BigDecimal sumDiscountedPrice) {
        this.sumDiscountedPrice = sumDiscountedPrice;
    }

    public void setSumCharge(BigDecimal sumCharge) {
        this.sumCharge = sumCharge;
    }

    public void setAvgQuantity(BigDecimal avgQuantity) {
        this.avgQuantity = avgQuantity;
    }

    public void setAvgPrice(BigDecimal avgPrice) {
        this.avgPrice = avgPrice;
    }

    public void setAvgDisc(BigDecimal avgDisc) {
        this.avgDisc = avgDisc;
    }

    public void setCountOrder(Long countOrder) {
        this.countOrder = countOrder;
    }
}
