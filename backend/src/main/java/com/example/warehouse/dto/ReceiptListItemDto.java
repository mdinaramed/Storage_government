package com.example.warehouse.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ReceiptListItemDto {
    private Long id;
    private String number;
    private LocalDate date;
}