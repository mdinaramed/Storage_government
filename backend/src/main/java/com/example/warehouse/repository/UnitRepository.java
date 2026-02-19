package com.example.warehouse.repository;

import com.example.warehouse.entity.EntityState;
import com.example.warehouse.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UnitRepository extends JpaRepository<Unit, Long> {
    boolean existsByNameIgnoreCase(String name);
    List<Unit> findAllByState(EntityState state);
}