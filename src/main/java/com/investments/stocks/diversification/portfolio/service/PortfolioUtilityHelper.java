package com.investments.stocks.diversification.portfolio.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.investments.stocks.diversification.portfolio.data.MarketCapAllocation;
import com.loan.service.LoanService;
import com.protection.insurance.service.InsuranceService;
import com.savings.data.FixedDepositDTO;
import com.savings.data.RecurringDepositDTO;
import com.savings.data.SavingsAccountDTO;
import com.savings.service.FixedDepositService;
import com.savings.service.RecurringDepositService;
import com.savings.service.SavingsAccountService;

@Component
public class PortfolioUtilityHelper {

    @Autowired
    private final LoanService loanService;
    private final InsuranceService insuranceService;
    private final RecurringDepositService recurringDepositService;
    private final FixedDepositService fixedDepositService;
    private final SavingsAccountService savingsAccountService;

    public PortfolioUtilityHelper(final LoanService loanService,
            final InsuranceService insuranceService,
            final RecurringDepositService recurringDepositService,
            final FixedDepositService fixedDepositService,
            final SavingsAccountService savingsAccountService) {
        this.loanService = loanService;
        this.insuranceService = insuranceService;
        this.recurringDepositService = recurringDepositService;
        this.fixedDepositService = fixedDepositService;
        this.savingsAccountService = savingsAccountService;
    }

    public BigDecimal calculateLoanOutstanding(Long uid) {
        BigDecimal loansOutstanding = BigDecimal.ZERO;
        try {
            if (uid != null) {
                loansOutstanding = loanService.getLoansByUserId(uid).stream()
                        .map(l -> l.getOutstandingAmount() != null ? l.getOutstandingAmount() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
        } catch (Exception ignored) {
        }
        return loansOutstanding;
    }

    public BigDecimal calculateInsuranceTotalCover(Long uid) {
        BigDecimal insuranceCover = BigDecimal.ZERO;
        try {
            if (uid != null) {
                insuranceCover = insuranceService.getInsurancePoliciesByUserId(uid).stream()
                        .map(i -> i.getCoverAmount() != null ? i.getCoverAmount() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
        } catch (Exception ignored) {
        }
        return insuranceCover;
    }

    public BigDecimal calculateRecurringDepositValue(Long uid) {
        BigDecimal rdValue = BigDecimal.ZERO;
        try {
            if (uid != null) {
                List<RecurringDepositDTO> rds = recurringDepositService.getAllRecurringDeposits(uid);
                if (rds != null) {
                    rdValue = rds.stream()
                            .map(rd -> rd.getMaturityAmount() != null ? rd.getMaturityAmount() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                }
            }
        } catch (Exception ignored) {
        }
        return rdValue;
    }

    public BigDecimal calculateFixedDepositValue(Long uid) {
        BigDecimal fdValue = BigDecimal.ZERO;
        try {
            if (uid != null) {
                List<FixedDepositDTO> fds = fixedDepositService.getAllFixedDeposits(uid);
                if (fds != null) {
                    fdValue = fds.stream()
                            .map(fd -> fd.getMaturityAmount() != null ? fd.getMaturityAmount() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                }
            }
        } catch (Exception ignored) {
        }
        return fdValue;
    }

    public BigDecimal calculateSavingsValue(Long uid) {
        BigDecimal savingsTotal = BigDecimal.ZERO;
        try {
            List<SavingsAccountDTO> savings = savingsAccountService.getAllSavingsAccounts(uid);
            if (savings != null) {
                savingsTotal = savings.stream()
                        .map(SavingsAccountDTO::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal recurringDepositValue = calculateRecurringDepositValue(uid);
                BigDecimal fixedDepositValue = calculateFixedDepositValue(uid);
                savingsTotal = savingsTotal.add(recurringDepositValue).add(fixedDepositValue);
            }
        } catch (Exception ignored) {
        }
        return savingsTotal;
    }

    public int calculateDiversificationScore(Map<String, BigDecimal> sectorAllocation) {
        if (sectorAllocation.isEmpty())
            return 0;

        int score = 100;
        int sectorCount = sectorAllocation.size();

        // Penalize for low sector count
        if (sectorCount == 1)
            score -= 50;
        else if (sectorCount == 2)
            score -= 30;
        else if (sectorCount == 3)
            score -= 10;

        // Find max sector weight
        double maxSectorWeight = sectorAllocation.values().stream()
                .mapToDouble(BigDecimal::doubleValue)
                .max().orElse(0.0);

        // Penalize for high concentration
        if (maxSectorWeight > 70.0)
            score -= 30;
        else if (maxSectorWeight > 50.0)
            score -= 20;
        else if (maxSectorWeight > 40.0)
            score -= 10;

        return Math.max(0, Math.min(100, score));
    }

    public String getAssessment(int score) {
        if (score >= 80)
            return "Well Diversified";
        if (score >= 60)
            return "Moderately Diversified";
        if (score >= 40)
            return "Concentrated";
        return "Needs Improvement";
    }

    public List<String> generateRecommendations(Map<String, BigDecimal> sectorAllocation, int score) {
        List<String> recs = new ArrayList<>();
        int sectorCount = sectorAllocation.size();

        if (sectorCount <= 2) {
            recs.add("Consider diversifying into more sectors.");
        }

        for (Map.Entry<String, BigDecimal> entry : sectorAllocation.entrySet()) {
            if (entry.getValue().doubleValue() > 50.0) {
                recs.add(String.format("High exposure to %s sector (%.2f%%). Consider reducing.", entry.getKey(),
                        entry.getValue()));
            }
        }

        if (sectorAllocation.containsKey("Unknown") && sectorAllocation.get("Unknown").doubleValue() > 10.0) {
            recs.add(
                    "A significant portion of your portfolio is mapped to 'Unknown' sector. Consider updating sector classifications.");
        }

        if (recs.isEmpty() && score < 50) {
            recs.add("Your portfolio is concentrated. Look for opportunities in new industries.");
        } else if (recs.isEmpty()) {
            recs.add("Your portfolio looks well diversified!");
        }

        return recs;
    }

    public MarketCapAllocation getMarketCapAllocation(BigDecimal currentValue, BigDecimal largeCapValue,
            BigDecimal midCapValue, BigDecimal smallCapValue) {

        if (currentValue.compareTo(BigDecimal.ZERO) > 0) {
            return new MarketCapAllocation(
                    largeCapValue.divide(currentValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)),
                    midCapValue.divide(currentValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)),
                    smallCapValue.divide(currentValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)));
        }
        return new MarketCapAllocation(largeCapValue, midCapValue, smallCapValue);
    }

}
