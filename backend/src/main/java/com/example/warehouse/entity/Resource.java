package com.example.warehouse.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resources", uniqueConstraints = {
        @UniqueConstraint(name = "uq_resource_name", columnNames = "name")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EntityState state = EntityState.ACTIVE;
}