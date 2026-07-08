package com.bupt.commerceinsight.query.vo;

import java.math.BigDecimal;

public class CustomerQueryVO {

    private final Long customerKey;
    private final String customerName;
    private final String nationName;
    private final BigDecimal accountBalance;
    private final String marketSegment;

    public CustomerQueryVO(Long customerKey, String customerName, String nationName, BigDecimal accountBalance, String marketSegment) {
        this.customerKey = customerKey;
        this.customerName = customerName;
        this.nationName = nationName;
        this.accountBalance = accountBalance;
        this.marketSegment = marketSegment;
    }

    public Long getCustomerKey() {
        return customerKey;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getNationName() {
        return nationName;
    }

    public BigDecimal getAccountBalance() {
        return accountBalance;
    }

    public String getMarketSegment() {
        return marketSegment;
    }
}
