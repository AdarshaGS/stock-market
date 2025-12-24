package com.investments.stocks.diversification.portfolio.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisInsight {

    private InsightType type;
    private InsightCategory category;
    private Integer priority; // 1 = High, 2 = Medium, 3 = Low
    private String message;
    private String recommendedAction;

    public enum InsightType {
        CRITICAL,
        WARNING,
        OPPORTUNITY,
        INFO
    }

    public enum InsightCategory {
        STOCK_CONCENTRATION,
        SECTOR_CONCENTRATION,
        MARKET_CAP_RISK,
        PERFORMANCE_DRAWDOWN,
        LIQUIDITY_RISK,
        INSURANCE_RISK
    }
}
