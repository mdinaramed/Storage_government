package com.example.warehouse.dto;

import com.example.warehouse.entity.EntityState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UnitDto {
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be <= 100 chars")
    private String name;

    private EntityState state;

    public UnitDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public EntityState getState() { return state; }
    public void setState(EntityState state) { this.state = state; }
}