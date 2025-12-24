package com.investments.stocks.diversification.portfolio.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.investments.stocks.diversification.portfolio.data.AnalysisInsight;
import com.investments.stocks.diversification.portfolio.data.NextBestAction;
import com.investments.stocks.diversification.portfolio.data.PortfolioInsightsDTO;
import com.investments.stocks.diversification.portfolio.data.RiskSummary;

@Service
public class PortfolioInsightService {

    public PortfolioInsightsDTO groupInsights(List<AnalysisInsight> insights) {
        // Sort insights by priority (if present) then by type
        List<AnalysisInsight> sortedInsights = insights.stream()
                .sorted(Comparator.comparing(AnalysisInsight::getPriority,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        Map<AnalysisInsight.InsightType, List<AnalysisInsight>> grouped = sortedInsights.stream()
                .collect(Collectors.groupingBy(AnalysisInsight::getType));

        return PortfolioInsightsDTO.builder()
                .critical(grouped.getOrDefault(AnalysisInsight.InsightType.CRITICAL, new ArrayList<>()))
                .warning(grouped.getOrDefault(AnalysisInsight.InsightType.WARNING, new ArrayList<>()))
                .info(grouped.getOrDefault(AnalysisInsight.InsightType.INFO, new ArrayList<>()))
                .build();
    }

    public RiskSummary calculateRiskSummary(List<AnalysisInsight> insights) {
        int critical = 0;
        int warning = 0;
        int info = 0;

        for (AnalysisInsight insight : insights) {
            switch (insight.getType()) {
                case CRITICAL:
                    critical++;
                    break;
                case WARNING:
                    warning++;
                    break;
                case INFO:
                case OPPORTUNITY:
                    info++;
                    break;
                default:
                    break;
            }
        }

        return RiskSummary.builder()
                .criticalCount(critical)
                .warningCount(warning)
                .infoCount(info)
                .build();
    }

    public List<String> generateTopRecommendations(List<AnalysisInsight> insights) {
        return insights.stream()
                .filter(i -> i.getType() == AnalysisInsight.InsightType.CRITICAL)
                .sorted(Comparator.comparing(AnalysisInsight::getPriority,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(AnalysisInsight::getRecommendedAction)
                .filter(action -> action != null && !action.isEmpty())
                .distinct()
                .limit(3)
                .collect(Collectors.toList());
    }

    public NextBestAction deriveNextBestAction(List<AnalysisInsight> insights) {
        return insights.stream()
                .filter(i -> i.getType() == AnalysisInsight.InsightType.CRITICAL)
                .sorted(Comparator.comparing(AnalysisInsight::getPriority,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .findFirst()
                .map(i -> NextBestAction.builder()
                        .title(mapCategoryToTitle(i.getCategory()))
                        .description(i.getMessage())
                        .urgency(mapPriorityToUrgency(i.getPriority()))
                        .build())
                .orElse(null);
    }

    private String mapCategoryToTitle(AnalysisInsight.InsightCategory category) {
        if (category == null)
            return "Action Required";
        switch (category) {
            case STOCK_CONCENTRATION:
                return "Reduce Single Stock Exposure";
            case SECTOR_CONCENTRATION:
                return "Diversify Sector Allocation";
            case MARKET_CAP_RISK:
                return "Balance Market Cap";
            case PERFORMANCE_DRAWDOWN:
                return "Review Underperforming Assets";
            case LIQUIDITY_RISK:
                return "Improve Liquidity";
            case INSURANCE_RISK:
                return "Review Insurance Coverage";
            default:
                return "Portfolio Action";
        }
    }

    private String mapPriorityToUrgency(Integer priority) {
        if (priority == null)
            return "MEDIUM";
        if (priority == 1)
            return "HIGH";
        if (priority == 2)
            return "MEDIUM";
        return "LOW";
    }
}
