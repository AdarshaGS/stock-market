package com.portfolio.engine;

import com.aa.data.FIPayload;
import com.aa.data.FIType;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PortfolioEngine {

    public PortfolioMetrics computeMetrics(FIPayload payload) {
        BigDecimal netWorth = BigDecimal.ZERO;
        Map<String, BigDecimal> assetSplit = new HashMap<>();
        BigDecimal totalAssets = BigDecimal.ZERO;
        BigDecimal totalDebt = BigDecimal.ZERO;

        Map<FIType, List<Object>> data = payload.getFinancialData();

        // 1. Bank Accounts (Cash)
        if (data.containsKey(FIType.BANK_ACCOUNTS)) {
            BigDecimal cash = calculateSum(data.get(FIType.BANK_ACCOUNTS), "balance");
            assetSplit.put("CASH", cash);
            totalAssets = totalAssets.add(cash);
        }

        // 2. Mutual Funds (Equity)
        if (data.containsKey(FIType.MUTUAL_FUNDS)) {
            BigDecimal equity = calculateSum(data.get(FIType.MUTUAL_FUNDS), "currentValue");
            assetSplit.put("EQUITY", equity);
            totalAssets = totalAssets.add(equity);
        }

        // 3. Loans (Debt)
        if (data.containsKey(FIType.LOANS)) {
            totalDebt = calculateSum(data.get(FIType.LOANS), "outstandingAmount");
            assetSplit.put("DEBT", totalDebt); // Simplification: loans subtract from net worth, but here we track them
                                               // for split
        }

        netWorth = totalAssets.subtract(totalDebt);

        return PortfolioMetrics.builder()
                .netWorth(netWorth)
                .totalAssets(totalAssets)
                .totalLiabilities(totalDebt)
                .assetSplit(calculateAssetSplitPercentages(assetSplit, totalAssets.add(totalDebt)))
                .build();
    }

    private BigDecimal calculateSum(List<Object> items, String fieldName) {
        BigDecimal sum = BigDecimal.ZERO;
        for (Object item : items) {
            if (item instanceof Map) {
                Object val = ((Map<?, ?>) item).get(fieldName);
                if (val instanceof Number) {
                    sum = sum.add(BigDecimal.valueOf(((Number) val).doubleValue()));
                }
            }
        }
        return sum;
    }

    private Map<String, Double> calculateAssetSplitPercentages(Map<String, BigDecimal> split, BigDecimal total) {
        Map<String, Double> percentages = new HashMap<>();
        if (total.compareTo(BigDecimal.ZERO) == 0)
            return percentages;

        split.forEach((k, v) -> {
            double p = v.divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
            percentages.put(k, p);
        });
        return percentages;
    }

    @Data
    @Builder
    public static class PortfolioMetrics {
        private BigDecimal netWorth;
        private BigDecimal totalAssets;
        private BigDecimal totalLiabilities;
        private Map<String, Double> assetSplit;
    }
}
