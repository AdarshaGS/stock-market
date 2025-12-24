package com.investments.stocks.diversification.portfolio.service;

import com.investments.stocks.diversification.portfolio.data.PortfolioDTOResponse;

public interface PortfolioReadPlatformService {

    PortfolioDTOResponse getPortfolioSummary(Long userId);
}
