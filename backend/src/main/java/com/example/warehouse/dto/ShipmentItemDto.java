package com.example.warehouse.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ShipmentItemDto {
    @NotNull(message = "resourceId is required")
    private Long resourceId;

    @NotNull(message = "unitId is required")
    private Long unitId;

    @NotNull(message = "quantity is required")
    @DecimalMin(value = "0.001", message = "quantity must be > 0")
    private BigDecimal quantity;

    public ShipmentItemDto() {}

    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }

    public Long getUnitId() { return unitId; }
    public void setUnitId(Long unitId) { this.unitId = unitId; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
}