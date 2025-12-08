package com.stocks.diversification.portfolio.rules;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.stocks.data.Stock;
import com.stocks.diversification.portfolio.data.AnalysisInsight;
import com.stocks.diversification.portfolio.data.Portfolio;

@Component
public class UnderPerformerRule implements PortfolioAnalysisRule {

    private static final double LOSS_THRESHOLD_PERCENT = -10.0;
    private static final double SEVERE_LOSS_THRESHOLD_PERCENT = -20.0;

    @Override
    public List<AnalysisInsight> evaluate(List<Portfolio> portfolios, Map<String, Stock> stockData,
            Map<Long, String> sectorMap) {
        List<AnalysisInsight> insights = new ArrayList<>();

        for (Portfolio p : portfolios) {
            Stock stock = stockData.get(p.getStockSymbol().toUpperCase());
            if (stock == null || stock.getPrice() == null)
                continue;

            BigDecimal currentPrice = BigDecimal.valueOf(stock.getPrice());
            BigDecimal buyPrice = p.getPurchasePrice();

            // (Current - Buy) / Buy * 100
            BigDecimal plPercent = currentPrice.subtract(buyPrice)
                    .divide(buyPrice, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            if (plPercent.doubleValue() < SEVERE_LOSS_THRESHOLD_PERCENT) {
                insights.add(AnalysisInsight.builder()
                        .type(AnalysisInsight.InsightType.CRITICAL)
                        .category(AnalysisInsight.InsightCategory.STOCK_PERFORMANCE)
                        .message("Stock " + p.getStockSymbol() + " has crashed by "
                                + String.format("%.1f", plPercent.doubleValue()) + "%.")
                        .recommendedAction(
                                "Review immediately. Is the investment thesis still valid? Consider stop-loss.")
                        .build());
            } else if (plPercent.doubleValue() < LOSS_THRESHOLD_PERCENT) {
                insights.add(AnalysisInsight.builder()
                        .type(AnalysisInsight.InsightType.WARNING)
                        .category(AnalysisInsight.InsightCategory.STOCK_PERFORMANCE)
                        .message("Stock " + p.getStockSymbol() + " is down by "
                                + String.format("%.1f", plPercent.doubleValue()) + "%.")
                        .recommendedAction("Monitor closely. Avoid averaging down unless confident.")
                        .build());
            }
        }

        return insights;
    }
}
