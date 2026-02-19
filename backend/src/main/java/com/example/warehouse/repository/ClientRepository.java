package com.example.warehouse.repository;

import com.example.warehouse.entity.Client;
import com.example.warehouse.entity.EntityState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<Client> findByState(EntityState state);

    List<Client> findByNameContainingIgnoreCase(String name);

    List<Client> findByNameContainingIgnoreCaseAndState(String name, EntityState state);
}