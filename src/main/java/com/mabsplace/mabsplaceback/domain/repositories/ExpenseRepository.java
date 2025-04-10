package com.mabsplace.mabsplaceback.domain.repositories;

import com.mabsplace.mabsplaceback.domain.entities.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByExpenseDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Expense> findByCategoryId(Long categoryId);
    List<Expense> findByCreatedById(Long userId);

    List<Expense> findByIsRecurringTrueAndNextRecurrenceDateLessThanEqual(LocalDateTime date);
}
