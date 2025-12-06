package com.stocks.diversification.portfolio.service;

import org.springframework.stereotype.Service;

import com.stocks.diversification.portfolio.data.Portfolio;
import com.stocks.diversification.portfolio.repo.PortfolioRepository;


@Service
public class PortfolioWritePlatformServiceImpl implements PortfolioWritePlatformService {

    private final PortfolioRepository portfolioRepository;

    public PortfolioWritePlatformServiceImpl(PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;
    }

    @Override
    public Portfolio addPortfolio(Portfolio portfolio) {
        Portfolio portfolioBuilder = Portfolio.builder()
                .userId(portfolio.getUserId())
                .stockSymbol(portfolio.getStockSymbol())
                .quantity(portfolio.getQuantity())
                .purchasePrice(portfolio.getPurchasePrice())
                .build();
      return this.portfolioRepository.save(portfolioBuilder);
    }
    
}
