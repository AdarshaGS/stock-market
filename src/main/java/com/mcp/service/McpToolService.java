package com.mcp.service;

import com.investments.stocks.diversification.portfolio.data.AnalysisInsight;
import com.investments.stocks.diversification.portfolio.data.PortfolioDTOResponse;
import com.investments.stocks.diversification.portfolio.service.PortfolioReadPlatformService;
import com.investments.stocks.networth.data.NetWorthDTO;
import com.investments.stocks.networth.service.NetWorthReadPlatformService;
import com.mcp.dto.McpNetWorthSummary;
import com.mcp.dto.McpPortfolioSummary;
import com.mcp.dto.McpRiskInsights;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class McpToolService {

    private final PortfolioReadPlatformService portfolioReadPlatformService;
    private final NetWorthReadPlatformService netWorthReadPlatformService;

    public McpPortfolioSummary getPortfolioSummary(Long userId) {
        PortfolioDTOResponse response = portfolioReadPlatformService.getPortfolioSummary(userId);
        return McpPortfolioSummary.builder()
                .score(response.getScore())
                .assessment(response.getAssessment())
                .sectorAllocation(response.getSectorAllocation())
                .marketCapAllocation(response.getMarketCapAllocation())
                .insights(response.getInsights())
                .nextBestAction(response.getNextBestAction())
                .scoreExplanation(response.getScoreExplanation())
                .dataFreshness(response.getDataFreshness())
                .build();
    }

    public McpNetWorthSummary getNetWorthSummary(Long userId) {
        NetWorthDTO response = netWorthReadPlatformService.getNetWorth(userId);
        Map<String, BigDecimal> breakdown = new HashMap<>();
        if (response.getAssetBreakdown() != null) {
            response.getAssetBreakdown().forEach((k, v) -> breakdown.put(k.name(), v));
        }
        if (response.getLiabilityBreakdown() != null) {
            response.getLiabilityBreakdown().forEach((k, v) -> breakdown.put(k.name(), v));
        }

        return McpNetWorthSummary.builder()
                .totalAssets(response.getTotalAssets())
                .totalLiabilities(response.getTotalLiabilities())
                .netWorth(response.getNetWorth())
                .breakdown(breakdown)
                .build();
    }

    public McpRiskInsights getRiskInsights(Long userId) {
        PortfolioDTOResponse response = portfolioReadPlatformService.getPortfolioSummary(userId);

        long criticalCount = 0;
        List<String> topInsights = List.of();

        if (response.getInsights() != null) {
            criticalCount = response.getInsights().getCritical().size();
            topInsights = response.getInsights().getCritical().stream()
                    .sorted(Comparator.comparing(AnalysisInsight::getPriority,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .map(AnalysisInsight::getMessage)
                    .limit(3)
                    .collect(Collectors.toList());
        }

        String riskLevel = "LOW";
        if (criticalCount > 3 || response.getScore() < 50) {
            riskLevel = "HIGH";
        } else if (criticalCount > 0 || response.getScore() < 75) {
            riskLevel = "MEDIUM";
        }

        return McpRiskInsights.builder()
                .overallRiskLevel(riskLevel)
                .criticalRiskCount(criticalCount)
                .topCriticalInsights(topInsights)
                .build();
    }
}
