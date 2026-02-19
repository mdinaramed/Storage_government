package com.example.warehouse.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "clients", uniqueConstraints = {
        @UniqueConstraint(name = "uq_client_name", columnNames = "name")
})
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = true, length = 255)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EntityState state = EntityState.ACTIVE;

    public Client() {}

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public EntityState getState() { return state; }
    public void setState(EntityState state) { this.state = state; }
}