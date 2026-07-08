package com.bupt.commerceinsight.query.vo;

import java.math.BigDecimal;

public class OrderRevenueVO {

    private final Long orderKey;
    private final String orderDate;
    private final String customerName;
    private final BigDecimal revenue;

    public OrderRevenueVO(Long orderKey, String orderDate, String customerName, BigDecimal revenue) {
        this.orderKey = orderKey;
        this.orderDate = orderDate;
        this.customerName = customerName;
        this.revenue = revenue;
    }

    public Long getOrderKey() {
        return orderKey;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public String getCustomerName() {
        return customerName;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }
}
