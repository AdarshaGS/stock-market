package com.budget.data;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class BudgetReportDTO {
    private String monthYear;
    private BigDecimal totalBudget;
    private BigDecimal totalSpent;
    private Map<ExpenseCategory, CategorySummary> categoryBreakdown;

    @Data
    @Builder
    public static class CategorySummary {
        private BigDecimal limit;
        private BigDecimal spent;
        private BigDecimal remaining;
        private double percentageUsed;
    }
}
