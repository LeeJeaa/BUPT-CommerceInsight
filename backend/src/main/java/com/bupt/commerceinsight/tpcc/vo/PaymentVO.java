package com.bupt.commerceinsight.tpcc.vo;

import java.math.BigDecimal;

public class PaymentVO {

    private final String transactionId;
    private final String status;
    private final Long customerId;
    private final BigDecimal newBalance;
    private final Long elapsedMs;

    public PaymentVO(String transactionId, String status, Long customerId, BigDecimal newBalance, Long elapsedMs) {
        this.transactionId = transactionId;
        this.status = status;
        this.customerId = customerId;
        this.newBalance = newBalance;
        this.elapsedMs = elapsedMs;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getStatus() {
        return status;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public BigDecimal getNewBalance() {
        return newBalance;
    }

    public Long getElapsedMs() {
        return elapsedMs;
    }
}
