package com.OdysseyAi.demo.trip.repository;

import com.OdysseyAi.demo.auth.entity.User;
import com.OdysseyAi.demo.common.enums.ExpenseCategory;
import com.OdysseyAi.demo.trip.entity.Expense;
import com.OdysseyAi.demo.trip.entity.Trip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ExpenseRepository
        extends JpaRepository<Expense, Long> {

    Page<Expense> findAllByTrip(
            Trip trip,
            Pageable pageable
    );

    Page<Expense> findAllByTripAndCategory(
            Trip trip,
            ExpenseCategory category,
            Pageable pageable
    );

    Page<Expense> findAllByTripAndPaidBy(
            Trip trip,
            User paidBy,
            Pageable pageable
    );

    Page<Expense> findAllByTripAndExpenseDateBetween(
            Trip trip,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    Page<Expense> findAllByTripAndCategoryAndExpenseDateBetween(
            Trip trip,
            ExpenseCategory category,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );
    Optional<Expense> findByIdAndTrip(
            Long id,
            Trip trip
    );

    void deleteByTrip(Trip trip);
}