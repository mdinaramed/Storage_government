package com.example.warehouse.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "units",
        uniqueConstraints = @UniqueConstraint(name = "uq_unit_name", columnNames = "name")
)
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EntityState state = EntityState.ACTIVE;

    public Unit() {}

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public EntityState getState() { return state; }
    public void setState(EntityState state) { this.state = state; }
}