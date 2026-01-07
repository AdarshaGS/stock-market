package com.investments.mutualfunds.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.investments.mutualfunds.data.MFAssetType;
import com.investments.mutualfunds.data.MFTransaction;
import com.investments.mutualfunds.data.MutualFundHolding;
import com.investments.mutualfunds.data.MutualFundInsights;
import com.investments.mutualfunds.data.MutualFundSummary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MutualFundServiceImpl implements MutualFundService {

    private final MutualFundFetchService fetchService;

    @Override
    public List<MutualFundHolding> getHoldings(Long userId) {
        return fetchService.fetchPortfolio(userId);
    }

    @Override
    public MutualFundSummary getSummary(Long userId) {
        List<MutualFundHolding> holdings = fetchService.fetchPortfolio(userId);
        
        BigDecimal totalCurrentValue = holdings.stream()
                .map(MutualFundHolding::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCostValue = holdings.stream()
                .map(MutualFundHolding::getCostValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalUnrealizedGain = totalCurrentValue.subtract(totalCostValue);
        
        double xirr = calculatePortfolioXirr(holdings);

        return MutualFundSummary.builder()
                .totalCurrentValue(totalCurrentValue)
                .totalCostValue(totalCostValue)
                .totalUnrealizedGain(totalUnrealizedGain)
                .xirr(xirr)
                .build();
    }

    @Override
    public MutualFundInsights getInsights(Long userId) {
        List<MutualFundHolding> holdings = fetchService.fetchPortfolio(userId);

        // Asset Allocation
        Map<String, BigDecimal> assetAllocation = new HashMap<>();
        for (MutualFundHolding h : holdings) {
            String type = h.getAssetType().name();
            assetAllocation.put(type, assetAllocation.getOrDefault(type, BigDecimal.ZERO).add(h.getCurrentValue()));
        }

        // AMC Concentration
        Map<String, BigDecimal> amcConcentration = new HashMap<>();
        for (MutualFundHolding h : holdings) {
            String amc = h.getAmc();
            amcConcentration.put(amc, amcConcentration.getOrDefault(amc, BigDecimal.ZERO).add(h.getCurrentValue()));
        }

        // Risk Buckets (Simplified: Equity=High, Debt=Low, Hybrid=Medium)
        Map<String, BigDecimal> riskBuckets = new HashMap<>();
        for (MutualFundHolding h : holdings) {
            String risk = getRiskBucket(h.getAssetType());
            riskBuckets.put(risk, riskBuckets.getOrDefault(risk, BigDecimal.ZERO).add(h.getCurrentValue()));
        }
        
        double xirr = calculatePortfolioXirr(holdings);

        return MutualFundInsights.builder()
                .assetAllocation(assetAllocation)
                .amcConcentration(amcConcentration)
                .riskBuckets(riskBuckets)
                .portfolioXirr(xirr)
                .build();
    }

    private String getRiskBucket(MFAssetType type) {
        switch (type) {
            case EQUITY: return "HIGH";
            case HYBRID: return "MODERATE";
            case DEBT: return "LOW";
            default: return "UNKNOWN";
        }
    }

    private double calculatePortfolioXirr(List<MutualFundHolding> holdings) {
        List<XirrCalculator.CashFlow> cashFlows = new ArrayList<>();
        
        for (MutualFundHolding holding : holdings) {
            // Add transactions as cash flows
            if (holding.getTransactions() != null) {
                for (MFTransaction txn : holding.getTransactions()) {
                    // Outflow (Buy) is negative, Inflow (Sell) is positive
                    double amount = txn.getAmount().doubleValue();
                    if ("BUY".equalsIgnoreCase(txn.getTxnType()) || "SIP".equalsIgnoreCase(txn.getTxnType())) {
                        amount = -amount;
                    }
                    cashFlows.add(new XirrCalculator.CashFlow(txn.getTxnDate(), amount));
                }
            }
            // Add current value as a final positive cash flow (terminal value)
            cashFlows.add(new XirrCalculator.CashFlow(java.time.LocalDate.now(), holding.getCurrentValue().doubleValue()));
        }

        if (cashFlows.isEmpty()) return 0.0;
        
        // Sort cashflows by date
        cashFlows.sort(java.util.Comparator.comparing(cf -> cf.date));

        try {
            return XirrCalculator.calculate(cashFlows);
        } catch (Exception e) {
            log.error("Error calculating XIRR", e);
            return 0.0;
        }
    }
}
