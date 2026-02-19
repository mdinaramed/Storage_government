package com.example.warehouse.repository;

import com.example.warehouse.entity.ShipmentItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentItemRepository extends JpaRepository<ShipmentItem, Long> {
    boolean existsByResourceId(Long resourceId);
    boolean existsByUnitId(Long unitId);
}