package com.example.warehouse.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class ReceiptDto {
    private Long id;

    @NotBlank(message = "Number is required")
    @Size(max = 50, message = "Number must be <= 50 chars")
    private String number;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @Valid
    private List<ReceiptItemDto> items = new ArrayList<>();
}