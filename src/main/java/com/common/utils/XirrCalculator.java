package com.common.utils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class XirrCalculator {

    public static class CashFlow {
        public final LocalDate date;
        public final double amount;

        public CashFlow(LocalDate date, double amount) {
            this.date = date;
            this.amount = amount;
        }
    }

    public static double calculate(List<CashFlow> cashFlows) {
        if (cashFlows.size() < 2) return 0.0;

        double x0 = 0.1; // Initial guess 10%
        double x1;
        
        for (int i = 0; i < 100; i++) {
            double f = 0;
            double df = 0;
            for (CashFlow cf : cashFlows) {
                double days = ChronoUnit.DAYS.between(cashFlows.get(0).date, cf.date);
                double t = days / 365.0;
                f += cf.amount / Math.pow(1 + x0, t);
                df -= t * cf.amount / Math.pow(1 + x0, t + 1);
            }
            x1 = x0 - f / df;
            if (Math.abs(x1 - x0) < 0.0001) {
                return x1 * 100; // Return as percentage
            }
            x0 = x1;
        }
        return x0 * 100;
    }
}
