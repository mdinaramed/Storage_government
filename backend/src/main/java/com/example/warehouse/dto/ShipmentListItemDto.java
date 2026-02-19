package com.example.warehouse.dto;

import com.example.warehouse.entity.ShipmentState;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ShipmentListItemDto {
    private Long id;
    private String number;
    private LocalDate date;
    private Long clientId;
    private String clientName;
    private ShipmentState state;
}