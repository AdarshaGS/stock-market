package com.investments.stocks.diversification.portfolio.service;

import org.springframework.stereotype.Service;

import com.investments.stocks.data.Stock;
import com.investments.stocks.diversification.portfolio.data.Portfolio;
import com.investments.stocks.diversification.portfolio.repo.PortfolioRepository;
import com.investments.stocks.diversification.sectors.repo.SectorRepository;
import com.investments.stocks.exception.SymbolNotFoundException;
import com.investments.stocks.repo.StockRepository;


@Service
public class PortfolioWritePlatformServiceImpl implements PortfolioWritePlatformService {

    private final PortfolioRepository portfolioRepository;
    private final StockRepository stockRepository;

    public PortfolioWritePlatformServiceImpl(PortfolioRepository portfolioRepository, StockRepository stockRepository) {
        this.portfolioRepository = portfolioRepository;
        this.stockRepository = stockRepository;
    }

    @Override
    public Portfolio addPortfolio(Portfolio portfolio) {

        Long stockId = this.stockRepository.findIdBySymbol(portfolio.getStockSymbol());
        if (stockId == null) {
            throw new SymbolNotFoundException("Symbol not found: " + portfolio.getStockSymbol());
        }
        
        Portfolio portfolioBuilder = Portfolio.builder()
                .userId(portfolio.getUserId())
                .stockId(stockId)
                .quantity(portfolio.getQuantity())
                .purchasePrice(portfolio.getPurchasePrice())
                .build();
      return this.portfolioRepository.save(portfolioBuilder);
    }
    
}
