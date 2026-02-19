package com.example.warehouse.repository;

import com.example.warehouse.entity.ReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiptItemRepository extends JpaRepository<ReceiptItem, Long> {
    boolean existsByResourceId(Long resourceId);
    boolean existsByUnitId(Long unitId);
}