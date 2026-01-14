package com.budget.api;

import com.budget.data.Budget;
import com.budget.data.BudgetReportDTO;
import com.budget.data.Expense;
import com.budget.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/v1/budget")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping("/expense")
    @PreAuthorize("@userSecurity.hasUserId(#expense.userId)")
    public Expense addExpense(@RequestBody Expense expense) {
        return budgetService.addExpense(expense);
    }

    @GetMapping("/expense/{userId}")
    @PreAuthorize("@userSecurity.hasUserId(#userId)")
    public List<Expense> getExpenses(@PathVariable("userId") Long userId) {
        return budgetService.getRecentExpenses(userId);
    }

    @PostMapping("/limit")
    @PreAuthorize("@userSecurity.hasUserId(#budget.userId)")
    public Budget setBudget(@RequestBody Budget budget) {
        return budgetService.setBudget(budget);
    }

    @GetMapping("/report/{userId}")
    @PreAuthorize("@userSecurity.hasUserId(#userId)")
    public BudgetReportDTO getReport(@PathVariable("userId") Long userId,
            @RequestParam(name = "monthYear", required = false) String monthYear) {
        return budgetService.getMonthlyReport(userId, monthYear);
    }
}
