package com.investments.stocks.diversification.portfolio.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.investments.stocks.diversification.portfolio.data.Portfolio;
import com.investments.stocks.diversification.portfolio.repo.PortfolioRepository;
import com.investments.stocks.exception.SymbolNotFoundException;
import com.investments.stocks.repo.StockRepository;
import com.investments.stocks.service.StockReadPlatformService;
import com.common.data.EntityType;

@Service
public class PortfolioWritePlatformServiceImpl implements PortfolioWritePlatformService {

    private final PortfolioRepository portfolioRepository;
    // private final StockRepository stockRepository;
    private final StockReadPlatformService stockReadPlatformService;

    public PortfolioWritePlatformServiceImpl(PortfolioRepository portfolioRepository, StockRepository stockRepository,
            StockReadPlatformService stockReadPlatformService) {
        this.portfolioRepository = portfolioRepository;
        // this.stockRepository = stockRepository;
        this.stockReadPlatformService = stockReadPlatformService;
    }

    @Override
    public Portfolio addPortfolio(Portfolio portfolio) {

        Long stockId = this.stockReadPlatformService.getStockBySymbol(portfolio.getEntityName()).getId();
        if (stockId == null) {
            throw new SymbolNotFoundException("Symbol not found: " + portfolio.getStockSymbol());
        }

        // calculate profit_and_loss percentage
        BigDecimal profitAndLoss = portfolio.getCurrentPrice().subtract(portfolio.getPurchasePrice());
        BigDecimal profitAndLossPercentage = profitAndLoss.divide(portfolio.getPurchasePrice(), 2,
                BigDecimal.ROUND_HALF_UP);

        Portfolio portfolioBuilder = Portfolio.builder()
                .userId(portfolio.getUserId())
                .entityType(portfolio.getEntityType() != null ? portfolio.getEntityType() : EntityType.STOCK)
                .stockId(stockId)
                .stockSymbol(portfolio.getStockSymbol())
                .quantity(portfolio.getQuantity())
                .purchasePrice(portfolio.getPurchasePrice())
                .currentPrice(portfolio.getCurrentPrice())
                .profitAndLossPercentage(profitAndLossPercentage)
                .build();
        return this.portfolioRepository.save(portfolioBuilder);
    }

}
