package com.mcp.server;

import com.mcp.context.McpContextProvider;
import com.mcp.dto.McpContext;
import com.mcp.dto.McpNetWorthSummary;
import com.mcp.dto.McpPortfolioSummary;
import com.mcp.dto.McpRiskInsights;
import com.mcp.policies.McpSafetyPolicy;
import com.mcp.service.McpToolService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/mcp")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class McpToolController {

    private static final Logger logger = LoggerFactory.getLogger(McpToolController.class);

    private final McpToolService mcpToolService;
    private final McpContextProvider mcpContextProvider;
    private final McpSafetyPolicy safetyPolicy;

    @GetMapping("/portfolio-summary/{userId}")
    public ResponseEntity<McpPortfolioSummary> getPortfolioSummary(@PathVariable Long userId) {
        return executeTool("getPortfolioSummary",
                () -> mcpToolService.getPortfolioSummary(userId));
    }

    @GetMapping("/net-worth-summary/{userId}")
    public ResponseEntity<McpNetWorthSummary> getNetWorthSummary(@PathVariable Long userId) {
        return executeTool("getNetWorthSummary",
                () -> mcpToolService.getNetWorthSummary(userId));
    }

    @GetMapping("/risk-insights/{userId}")
    public ResponseEntity<McpRiskInsights> getRiskInsights(@PathVariable Long userId) {
        return executeTool("getRiskInsights",
                () -> mcpToolService.getRiskInsights(userId));
    }

    @GetMapping("/context/{userId}")
    public ResponseEntity<McpContext> getContext(@PathVariable Long userId) {
        long startTime = System.currentTimeMillis();
        try {
            McpContext context = mcpContextProvider.getContext(userId);
            long duration = System.currentTimeMillis() - startTime;
            logger.info("MCP Context Built: userId={}, duration={}ms", userId, duration);
            return ResponseEntity.ok(context);
        } catch (Exception e) {
            logger.error("MCP Context Build Failed: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    private <T> ResponseEntity<T> executeTool(String toolName, ToolAction<T> action) {
        long startTime = System.currentTimeMillis();
        logger.info("MCP Tool Called: {}", toolName);

        try {
            safetyPolicy.enforceReadOnly();
            T result = action.execute();
            long duration = System.currentTimeMillis() - startTime;
            logger.info("MCP Tool Executed: {}, duration={}ms", toolName, duration);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("MCP Tool Failed: {}, duration={}ms, error={}", toolName, duration, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @FunctionalInterface
    private interface ToolAction<T> {
        T execute() throws Exception;
    }
}
