package com.stocks.diversification.portfolio.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stocks.diversification.portfolio.data.Portfolio;
import com.stocks.diversification.portfolio.data.PortfolioDTOResponse;
import com.stocks.diversification.portfolio.service.PortfolioReadPlatformService;
import com.stocks.diversification.portfolio.service.PortfolioWritePlatformService;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioAPIResource {

    private final PortfolioWritePlatformService portfolioWritePlatformService;
    private final PortfolioReadPlatformService portfolioReadPlatformService;

    public PortfolioAPIResource(final PortfolioWritePlatformService portfolioWritePlatformService,
            final PortfolioReadPlatformService portfolioReadPlatformService) {
        this.portfolioWritePlatformService = portfolioWritePlatformService;
        this.portfolioReadPlatformService = portfolioReadPlatformService;
    }

    // Define your API endpoints here
    // Post api to post portfolio data
    // each user will have multiple stocks in his portfolio, so user id will not be
    // unique
    @PostMapping()
    public Portfolio postPortfolioData(@RequestBody Portfolio portfolio) {
        return this.portfolioWritePlatformService.addPortfolio(portfolio);
    }

    @GetMapping("/diversification-score/{userId}")
    public PortfolioDTOResponse getDiversificationScore(@PathVariable Long userId) {
        return this.portfolioReadPlatformService.getDiversificationScore(userId);
    }

    @GetMapping("/summary/{userId}")
    public PortfolioDTOResponse getPortfolioSummary(@PathVariable Long userId) {
        return this.portfolioReadPlatformService.getPortfolioSummary(userId);
    }

}
