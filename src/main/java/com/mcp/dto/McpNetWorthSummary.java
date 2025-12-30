package com.mcp.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class McpNetWorthSummary {
    private BigDecimal totalAssets;
    private BigDecimal totalLiabilities;
    private BigDecimal netWorth;
    private Map<String, BigDecimal> breakdown;
}
