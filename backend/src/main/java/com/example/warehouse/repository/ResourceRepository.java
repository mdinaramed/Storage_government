package com.example.warehouse.repository;

import com.example.warehouse.entity.EntityState;
import com.example.warehouse.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
    boolean existsByNameIgnoreCase(String name);
    List<Resource> findAllByState(EntityState state);
}