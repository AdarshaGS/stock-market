package com.mcp.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class McpContext {
    private UserProfile userProfile;
    private PortfolioRiskState portfolioRiskState;
    private NetWorthSnapshot netWorthSnapshot;

    @Data
    @Builder
    public static class UserProfile {
        private Long userId;
        private String riskTolerance;
    }

    @Data
    @Builder
    public static class PortfolioRiskState {
        private int score;
        private String assessment;
        private long criticalCount;
        private String overallRiskLevel;
        private String nextBestAction;
    }

    @Data
    @Builder
    public static class NetWorthSnapshot {
        private BigDecimal netWorth;
        private BigDecimal assets;
        private BigDecimal liabilities;
    }
}
