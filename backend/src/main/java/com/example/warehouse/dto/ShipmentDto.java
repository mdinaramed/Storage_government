package com.example.warehouse.dto;

import com.example.warehouse.entity.ShipmentState;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ShipmentDto {
    private Long id;

    @NotBlank(message = "Number is required")
    @Size(max = 50, message = "Number must be <= 50 chars")
    private String number;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "clientId is required")
    private Long clientId;

    private ShipmentState state;

    @Valid
    private List<ShipmentItemDto> items = new ArrayList<>();

    public ShipmentDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public ShipmentState getState() { return state; }
    public void setState(ShipmentState state) { this.state = state; }

    public List<ShipmentItemDto> getItems() { return items; }
    public void setItems(List<ShipmentItemDto> items) {
        this.items = (items == null) ? new ArrayList<>() : items;
    }
}