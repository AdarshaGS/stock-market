package com.mcp.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class McpPortfolioSummary {
    private int score;
    private String assessment;
    private Object sectorAllocation;
    private Object marketCapAllocation;
    private Object insights;
    private Object nextBestAction;
    private Object scoreExplanation;
    private Object dataFreshness;
}
