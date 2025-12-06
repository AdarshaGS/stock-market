package com.stocks.diversification.portfolio.data;

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
    private String message;
    private String recommendedAction;

    public enum InsightType {
        CRITICAL,
        WARNING,
        OPPORTUNITY,
        INFO
    }

    public enum InsightCategory {
        SECTOR_ALLOCATION,
        STOCK_PERFORMANCE,
        DIVERSIFICATION,
        RISK
    }
}
