package com.example.warehouse.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class ReceiptListItemDto {
    private Long id;
    private String number;
    private LocalDate date;

    private List<Item> items = new ArrayList<>();

    @Data
    public static class Item {
        private Long resourceId;
        private String resourceName;
        private Long unitId;
        private String unitName;
        private BigDecimal quantity;
    }
}