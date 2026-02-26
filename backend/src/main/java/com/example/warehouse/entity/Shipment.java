package com.example.warehouse.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "shipments",
        uniqueConstraints = @UniqueConstraint(name = "uq_shipment_number", columnNames = "number")
)
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String number;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShipmentState state = ShipmentState.DRAFT;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShipmentItem> items = new ArrayList<>();

    public Shipment() {}

    public Long getId() { return id; }

    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public ShipmentState getState() { return state; }
    public void setState(ShipmentState state) { this.state = state; }

    public List<ShipmentItem> getItems() { return items; }

    public void setItems(List<ShipmentItem> newItems) {
        this.items.clear();
        if (newItems != null) {
            for (ShipmentItem it : newItems) {
                it.setShipment(this);
                this.items.add(it);
            }
        }
    }
}