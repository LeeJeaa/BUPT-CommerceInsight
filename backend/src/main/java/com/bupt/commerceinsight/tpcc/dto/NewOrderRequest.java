package com.bupt.commerceinsight.tpcc.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class NewOrderRequest {

    @NotNull
    @Min(1)
    private Long warehouseId;

    @NotNull
    @Min(1)
    private Long districtId;

    @NotNull
    @Min(1)
    private Long customerId;

    @Valid
    @NotEmpty
    private List<NewOrderItemRequest> items;

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public Long getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Long districtId) {
        this.districtId = districtId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public List<NewOrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<NewOrderItemRequest> items) {
        this.items = items;
    }
}
