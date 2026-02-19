package com.example.warehouse.repository;

import com.example.warehouse.entity.Balance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BalanceRepository extends JpaRepository<Balance, Long> {
    Optional<Balance> findByResourceIdAndUnitId(Long resourceId, Long unitId);
}