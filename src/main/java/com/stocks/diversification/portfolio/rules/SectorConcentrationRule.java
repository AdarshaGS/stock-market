package com.stocks.diversification.portfolio.rules;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.stocks.data.Stock;
import com.stocks.diversification.portfolio.data.AnalysisInsight;
import com.stocks.diversification.portfolio.data.Portfolio;

@Component
public class SectorConcentrationRule implements PortfolioAnalysisRule {

    private static final double MAX_SECTOR_ALLOCATION_PERCENT = 30.0;
    private static final double CRITICAL_SECTOR_ALLOCATION_PERCENT = 50.0;

    @Override
    public List<AnalysisInsight> evaluate(List<Portfolio> portfolios, Map<String, Stock> stockData,
            Map<Long, String> sectorMap) {
        List<AnalysisInsight> insights = new ArrayList<>();
        Map<String, BigDecimal> sectorValue = new HashMap<>();
        BigDecimal totalValue = BigDecimal.ZERO;

        // Calculate Sector Weights
        for (Portfolio p : portfolios) {
            Stock stock = stockData.get(p.getStockSymbol());
            // Use current price logic (handled in Service mostly, but good to double check)
            BigDecimal price = (stock != null && stock.getPrice() != null) ? BigDecimal.valueOf(stock.getPrice())
                    : p.getPurchasePrice();

            BigDecimal val = price.multiply(BigDecimal.valueOf(p.getQuantity()));
            totalValue = totalValue.add(val);

            String sectorName = "Others";
            if (stock != null && stock.getSectorId() != null) {
                sectorName = sectorMap.getOrDefault(stock.getSectorId(), "Others");
            }
            sectorValue.merge(sectorName, val, BigDecimal::add);
        }

        if (totalValue.compareTo(BigDecimal.ZERO) == 0)
            return insights;

        for (Map.Entry<String, BigDecimal> entry : sectorValue.entrySet()) {
            double percentage = entry.getValue().divide(totalValue, 4, RoundingMode.HALF_UP).doubleValue() * 100;

            if (percentage > CRITICAL_SECTOR_ALLOCATION_PERCENT) {
                insights.add(AnalysisInsight.builder()
                        .type(AnalysisInsight.InsightType.CRITICAL)
                        .category(AnalysisInsight.InsightCategory.SECTOR_ALLOCATION)
                        .message("Extreme concentration in " + entry.getKey() + " sector ("
                                + String.format("%.1f", percentage) + "%).")
                        .recommendedAction("Urgently diversify. Sell some positions in " + entry.getKey()
                                + " and buy into other sectors.")
                        .build());
            } else if (percentage > MAX_SECTOR_ALLOCATION_PERCENT) {
                insights.add(AnalysisInsight.builder()
                        .type(AnalysisInsight.InsightType.WARNING)
                        .category(AnalysisInsight.InsightCategory.SECTOR_ALLOCATION)
                        .message("High concentration in " + entry.getKey() + " sector ("
                                + String.format("%.1f", percentage) + "%).")
                        .recommendedAction("Consider reducing exposure to " + entry.getKey() + " below 30%.")
                        .build());
            }
        }

        return insights;
    }
}
