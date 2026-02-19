package com.example.warehouse.dto;

import java.math.BigDecimal;

public class BalanceDto {
    private Long id;
    private Long resourceId;
    private String resourceName;
    private Long unitId;
    private String unitName;
    private BigDecimal amount;

    public BalanceDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }

    public Long getUnitId() { return unitId; }
    public void setUnitId(Long unitId) { this.unitId = unitId; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}