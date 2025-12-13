package com.investments.stocks.diversification.portfolio.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.investments.stocks.diversification.portfolio.data.Portfolio;
import com.investments.stocks.diversification.portfolio.data.PortfolioDTOResponse;
import com.investments.stocks.diversification.portfolio.service.PortfolioReadPlatformService;
import com.investments.stocks.diversification.portfolio.service.PortfolioWritePlatformService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/portfolio")
@Tag(name = "Portfolio Management", description = "APIs for managing and analyzing portfolios")
public class PortfolioAPIResource {

    private final PortfolioWritePlatformService portfolioWritePlatformService;
    private final PortfolioReadPlatformService portfolioReadPlatformService;

    public PortfolioAPIResource(final PortfolioWritePlatformService portfolioWritePlatformService,
            final PortfolioReadPlatformService portfolioReadPlatformService) {
        this.portfolioWritePlatformService = portfolioWritePlatformService;
        this.portfolioReadPlatformService = portfolioReadPlatformService;
    }

    @PostMapping()
    @Operation(summary = "Add portfolio item", description = "Adds a stock to the user's portfolio.")
    @ApiResponse(responseCode = "200", description = "Successfully added portfolio item")
    public Portfolio postPortfolioData(@RequestBody Portfolio portfolio) {
        return this.portfolioWritePlatformService.addPortfolio(portfolio);
    }

    @GetMapping("/diversification-score/{userId}")
    @Operation(summary = "Get diversification score", description = "Calculates and returns the portfolio diversification score.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved diversification score")
    public PortfolioDTOResponse getDiversificationScore(@PathVariable Long userId) {
        return this.portfolioReadPlatformService.getDiversificationScore(userId);
    }

    @GetMapping("/summary/{userId}")
    @Operation(summary = "Get portfolio summary", description = "Returns a comprehensive summary including investment, value, and analysis.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved portfolio summary")
    public PortfolioDTOResponse getPortfolioSummary(@PathVariable Long userId) {
        return this.portfolioReadPlatformService.getPortfolioSummary(userId);
    }

}
