package com.mcp.context;

import com.investments.stocks.diversification.portfolio.data.PortfolioDTOResponse;
import com.investments.stocks.diversification.portfolio.service.PortfolioReadPlatformService;
import com.investments.stocks.networth.data.NetWorthDTO;
import com.investments.stocks.networth.service.NetWorthReadPlatformService;
import com.mcp.dto.McpContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class McpContextProvider {

    private final PortfolioReadPlatformService portfolioReadPlatformService;
    private final NetWorthReadPlatformService netWorthReadPlatformService;

    public McpContext getContext(Long userId) {
        PortfolioDTOResponse portfolio = portfolioReadPlatformService.getPortfolioSummary(userId);
        NetWorthDTO netWorth = netWorthReadPlatformService.getNetWorth(userId);

        long criticalCount = 0;
        if (portfolio.getInsights() != null) {
            criticalCount = portfolio.getInsights().getCritical().size();
        }

        String riskLevel = "LOW";
        if (criticalCount > 3 || portfolio.getScore() < 50) {
            riskLevel = "HIGH";
        } else if (criticalCount > 0 || portfolio.getScore() < 75) {
            riskLevel = "MEDIUM";
        }

        String nextAction = portfolio.getNextBestAction() != null ? portfolio.getNextBestAction().getDescription()
                : "No urgent action required";

        return McpContext.builder()
                .userProfile(McpContext.UserProfile.builder()
                        .userId(userId)
                        .riskTolerance("UNKNOWN") // Placeholder as per requirements
                        .build())
                .portfolioRiskState(McpContext.PortfolioRiskState.builder()
                        .score(portfolio.getScore())
                        .assessment(portfolio.getAssessment())
                        .criticalCount(criticalCount)
                        .overallRiskLevel(riskLevel)
                        .nextBestAction(nextAction)
                        .build())
                .netWorthSnapshot(McpContext.NetWorthSnapshot.builder()
                        .netWorth(netWorth.getNetWorth())
                        .assets(netWorth.getTotalAssets())
                        .liabilities(netWorth.getTotalLiabilities())
                        .build())
                .build();
    }
}
