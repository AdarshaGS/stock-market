package com.budget.service;

import com.budget.data.*;
import com.budget.repo.BudgetRepository;
import com.budget.repo.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;

    public Expense addExpense(Expense expense) {
        if (expense.getExpenseDate() == null) {
            expense.setExpenseDate(LocalDate.now());
        }
        return expenseRepository.save(expense);
    }

    public Budget setBudget(Budget budget) {
        if (budget.getMonthYear() == null) {
            budget.setMonthYear(YearMonth.now().toString());
        }
        if (budget.getCategory() == null) {
            budget.setCategory(ExpenseCategory.TOTAL);
        }
        return budgetRepository.findByUserIdAndCategoryAndMonthYear(
                budget.getUserId(), budget.getCategory(), budget.getMonthYear())
                .map(existing -> {
                    existing.setMonthlyLimit(budget.getMonthlyLimit());
                    return budgetRepository.save(existing);
                })
                .orElseGet(() -> budgetRepository.save(budget));
    }

    public BudgetReportDTO getMonthlyReport(Long userId, String monthYear) {
        if (monthYear == null) {
            monthYear = YearMonth.now().toString();
        }
        YearMonth ym = YearMonth.parse(monthYear);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<Expense> expenses = expenseRepository.findByUserIdAndExpenseDateBetween(userId, start, end);
        List<Budget> budgets = budgetRepository.findByUserIdAndMonthYear(userId, monthYear);

        Map<ExpenseCategory, BigDecimal> spentPerCategory = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)));

        Map<ExpenseCategory, BudgetReportDTO.CategorySummary> breakdown = new HashMap<>();

        for (Budget b : budgets) {
            if (b.getCategory() == ExpenseCategory.TOTAL) {
                continue;
            }
            BigDecimal spent = spentPerCategory.getOrDefault(b.getCategory(), BigDecimal.ZERO);
            BigDecimal limit = b.getMonthlyLimit();
            breakdown.put(b.getCategory(), BudgetReportDTO.CategorySummary.builder()
                    .limit(limit)
                    .spent(spent)
                    .remaining(limit.subtract(spent))
                    .percentageUsed(calculatePercentage(spent, limit))
                    .build());
        }

        // Add categories that have spending but no budget
        spentPerCategory.forEach((cat, spent) -> {
            if (!breakdown.containsKey(cat) && cat != ExpenseCategory.TOTAL) {
                breakdown.put(cat, BudgetReportDTO.CategorySummary.builder()
                        .limit(BigDecimal.ZERO)
                        .spent(spent)
                        .remaining(spent.negate())
                        .percentageUsed(100.0)
                        .build());
            }
        });

        BigDecimal totalSpent = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalBudget = budgets.stream()
                .filter(b -> b.getCategory() == ExpenseCategory.TOTAL)
                .map(Budget::getMonthlyLimit)
                .findFirst()
                .orElseGet(() -> budgets.stream()
                        .filter(b -> b.getCategory() != ExpenseCategory.TOTAL)
                        .map(Budget::getMonthlyLimit)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));

        return BudgetReportDTO.builder()
                .monthYear(monthYear)
                .totalBudget(totalBudget)
                .totalSpent(totalSpent)
                .categoryBreakdown(breakdown)
                .build();
    }

    private double calculatePercentage(BigDecimal spent, BigDecimal limit) {
        if (limit.compareTo(BigDecimal.ZERO) == 0)
            return 0.0;
        return spent.divide(limit, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .doubleValue();
    }

    public List<Expense> getRecentExpenses(Long userId) {
        return expenseRepository.findByUserId(userId);
    }
}
