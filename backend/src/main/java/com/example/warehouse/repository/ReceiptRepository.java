package com.example.warehouse.repository;

import com.example.warehouse.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

    boolean existsByNumberIgnoreCase(String number);

    @Query("select r from Receipt r where r.date between :from and :to")
    List<Receipt> findByDateBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("select r from Receipt r where lower(r.number) in :numbers")
    List<Receipt> findByNumberLowerIn(@Param("numbers") List<String> numbers);
}