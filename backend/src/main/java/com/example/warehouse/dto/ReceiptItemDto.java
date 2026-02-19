package com.example.warehouse.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReceiptItemDto {
    @NotNull(message = "resourceId is required")
    private Long resourceId;

    @NotNull(message = "unitId is required")
    private Long unitId;

    @NotNull(message = "quantity is required")
    @DecimalMin(value = "0.001", message = "quantity must be > 0")
    private BigDecimal quantity;
}