package com.example.warehouse.repository;

import com.example.warehouse.entity.Receipt;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

    boolean existsByNumberIgnoreCase(String number);

    @EntityGraph(attributePaths = {"items", "items.resource", "items.unit"})
    @Query("select distinct r from Receipt r")
    List<Receipt> findAllWithItems();
}