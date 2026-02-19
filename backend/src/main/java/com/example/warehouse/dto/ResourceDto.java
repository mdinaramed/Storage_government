package com.example.warehouse.dto;

import com.example.warehouse.entity.EntityState;

public class ResourceDto {
    private Long id;
    private String name;
    private EntityState state;

    public ResourceDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public EntityState getState() { return state; }
    public void setState(EntityState state) { this.state = state; }
}