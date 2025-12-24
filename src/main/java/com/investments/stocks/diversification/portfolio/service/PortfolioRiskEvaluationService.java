package com.investments.stocks.diversification.portfolio.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.investments.stocks.data.Stock;
import com.investments.stocks.diversification.portfolio.data.AnalysisInsight;
import com.investments.stocks.diversification.portfolio.data.Portfolio;
import com.investments.stocks.diversification.portfolio.data.RiskAnalysisResult;

@Service
public class PortfolioRiskEvaluationService {

    public RiskAnalysisResult evaluateRisks(List<Portfolio> portfolios, Map<String, Stock> stockMap,
            BigDecimal totalValue, Map<String, BigDecimal> sectorAllocation, BigDecimal smallCapAllocationPct) {

        List<AnalysisInsight> insights = new ArrayList<>();
        double maxStockPct = 0.0;
        boolean hasDrawdown = false;

        // 1. Stock Concentration & Drawdown
        if (totalValue.compareTo(BigDecimal.ZERO) > 0) {
            for (Portfolio p : portfolios) {
                Stock stock = stockMap.get(p.getStockSymbol().toUpperCase());
                BigDecimal price = (stock != null && stock.getPrice() != null) ? BigDecimal.valueOf(stock.getPrice())
                        : (p.getCurrentPrice() != null ? p.getCurrentPrice() : p.getPurchasePrice());

                BigDecimal curVal = BigDecimal.valueOf(p.getQuantity()).multiply(price);
                double pct = curVal.divide(totalValue, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;

                if (pct > maxStockPct) {
                    maxStockPct = pct;
                }

                // Concentration Checks
                if (pct > 40.0) {
                    insights.add(createInsight(AnalysisInsight.InsightType.CRITICAL,
                            AnalysisInsight.InsightCategory.STOCK_CONCENTRATION,
                            1, // Priority 1: Fix immediately
                            String.format("Critical exposure to single stock: %s (%.1f%%)", p.getStockSymbol(), pct),
                            "Reduce position size immediately to below 10%."));
                } else if (pct > 25.0) {
                    insights.add(createInsight(AnalysisInsight.InsightType.CRITICAL,
                            AnalysisInsight.InsightCategory.STOCK_CONCENTRATION,
                            2, // Priority 2: Important
                            String.format("High exposure to single stock: %s (%.1f%%)", p.getStockSymbol(), pct),
                            "Consider trimming position to reduce risk."));
                } else if (pct > 10.0) {
                    insights.add(createInsight(AnalysisInsight.InsightType.WARNING,
                            AnalysisInsight.InsightCategory.STOCK_CONCENTRATION,
                            3, // Priority 3: Monitor
                            String.format("Concentrated position in %s (%.1f%%)", p.getStockSymbol(), pct),
                            "Keep single stock exposure below 10% for better diversification."));
                }

                // Drawdown Check
                // Drawdown = (Current - Buy) / Buy
                BigDecimal buyPrice = p.getPurchasePrice();
                if (buyPrice.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal diff = price.subtract(buyPrice);
                    double drawdown = diff.divide(buyPrice, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
                    if (drawdown < -25.0) {
                        hasDrawdown = true;
                        insights.add(createInsight(AnalysisInsight.InsightType.CRITICAL,
                                AnalysisInsight.InsightCategory.PERFORMANCE_DRAWDOWN,
                                1, // Priority 1: Stock crash is urgent
                                String.format("Stock crash alert: %s is down %.1f%%", p.getStockSymbol(), drawdown),
                                "Review fundamentals. Consider stop-loss or holding if long-term thesis is intact."));
                    }
                }
            }
        }

        // 2. Sector Concentration
        double maxSectorPct = 0.0;
        for (Map.Entry<String, BigDecimal> entry : sectorAllocation.entrySet()) {
            double pct = entry.getValue().doubleValue();
            if (pct > maxSectorPct) {
                maxSectorPct = pct;
            }

            if (pct > 40.0) {
                insights.add(createInsight(AnalysisInsight.InsightType.CRITICAL,
                        AnalysisInsight.InsightCategory.SECTOR_CONCENTRATION,
                        2, // Priority 2
                        String.format("Critical sector over-allocation: %s (%.1f%%)", entry.getKey(), pct),
                        "Diversify into other sectors to reduce systemic risk."));
            } else if (pct > 30.0) {
                insights.add(createInsight(AnalysisInsight.InsightType.WARNING,
                        AnalysisInsight.InsightCategory.SECTOR_CONCENTRATION,
                        3, // Priority 3
                        String.format("High sector exposure: %s (%.1f%%)", entry.getKey(), pct),
                        "Limit sector exposure to 30% maximum."));
            }
        }

        // 3. Small Cap Risk
        double smallCapPct = smallCapAllocationPct.doubleValue();
        if (smallCapPct > 70.0) {
            insights.add(createInsight(AnalysisInsight.InsightType.CRITICAL,
                    AnalysisInsight.InsightCategory.MARKET_CAP_RISK,
                    2, // Priority 2
                    String.format("High Small-Cap Exposure (%.1f%%)", smallCapPct),
                    "Rebalance portfolio with Large/Mid-cap stocks for stability."));
        } else if (smallCapPct > 50.0) { // Optional warning
            insights.add(createInsight(AnalysisInsight.InsightType.WARNING,
                    AnalysisInsight.InsightCategory.MARKET_CAP_RISK,
                    3, // Priority 3
                    String.format("Significant Small-Cap Exposure (%.1f%%)", smallCapPct),
                    "Monitor volatility."));
        }

        return RiskAnalysisResult.builder()
                .insights(insights)
                .maxStockAllocation(maxStockPct)
                .maxSectorAllocation(maxSectorPct)
                .smallCapAllocation(smallCapPct)
                .hasSignificantDrawdown(hasDrawdown)
                .build();
    }

    private AnalysisInsight createInsight(AnalysisInsight.InsightType type, AnalysisInsight.InsightCategory category,
            Integer priority,
            String message, String action) {
        return AnalysisInsight.builder()
                .type(type)
                .category(category)
                .priority(priority)
                .message(message)
                .recommendedAction(action)
                .build();
    }
}
