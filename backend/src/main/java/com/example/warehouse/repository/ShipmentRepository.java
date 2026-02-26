package com.example.warehouse.repository;

import com.example.warehouse.entity.Shipment;
import com.example.warehouse.entity.ShipmentState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    boolean existsByNumberIgnoreCase(String number);

    List<Shipment> findAllByDateBetween(LocalDate from, LocalDate to);
    List<Shipment> findAllByState(ShipmentState state);

    @Query("select distinct s.number from Shipment s where s.number is not null order by s.number")
    List<String> findDistinctNumbers();
}