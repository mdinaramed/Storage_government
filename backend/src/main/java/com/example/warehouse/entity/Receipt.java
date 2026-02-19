package com.example.warehouse.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "receipts", uniqueConstraints = {
        @UniqueConstraint(name = "uq_receipt_number", columnNames = "number")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String number;

    @Column(nullable = false)
    private LocalDate date;

    @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReceiptItem> items = new ArrayList<>();

    public void setItems(List<ReceiptItem> newItems) {
        this.items.clear();
        if (newItems != null) {
            for (ReceiptItem it : newItems) {
                it.setReceipt(this);
                this.items.add(it);
            }
        }
    }
}