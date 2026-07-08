package com.bupt.commerceinsight.tpcc.vo;

import java.math.BigDecimal;

public class NewOrderVO {

    private final String transactionId;
    private final String status;
    private final Long orderId;
    private final BigDecimal totalAmount;
    private final Long elapsedMs;

    public NewOrderVO(String transactionId, String status, Long orderId, BigDecimal totalAmount, Long elapsedMs) {
        this.transactionId = transactionId;
        this.status = status;
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.elapsedMs = elapsedMs;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getStatus() {
        return status;
    }

    public Long getOrderId() {
        return orderId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Long getElapsedMs() {
        return elapsedMs;
    }
}
