package com.mcp.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class McpRiskInsights {
    private String overallRiskLevel;
    private long criticalRiskCount;
    private List<String> topCriticalInsights;
}
