package com.example.warehouse.dto;

import com.example.warehouse.entity.EntityState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ClientDto {
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must be <= 200 chars")
    private String name;

    @Size(max = 500, message = "Address must be <= 500 chars")
    private String address;

    private EntityState state;

    public ClientDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public EntityState getState() { return state; }
    public void setState(EntityState state) { this.state = state; }
}