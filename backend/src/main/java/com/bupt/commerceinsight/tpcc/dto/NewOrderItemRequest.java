package com.bupt.commerceinsight.tpcc.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class NewOrderItemRequest {

    @NotNull
    private Long itemId;

    @NotNull
    @Min(1)
    private Integer quantity;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
