package com.budget.repo;

import com.budget.data.Expense;
import com.budget.data.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUserId(Long userId);

    List<Expense> findByUserIdAndExpenseDateBetween(Long userId, LocalDate start, LocalDate end);

    List<Expense> findByUserIdAndCategoryAndExpenseDateBetween(Long userId, ExpenseCategory category, LocalDate start,
            LocalDate end);
}
