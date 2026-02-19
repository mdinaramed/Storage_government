package com.example.warehouse.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "balances",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_balance_resource_unit",
                columnNames = {"resource_id", "unit_id"}
        )
)
public class Balance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount = BigDecimal.ZERO;

    public Balance() {}

    public Long getId() {
        return id;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = (amount == null) ? BigDecimal.ZERO : amount;
    }
}