package com.stocks.diversification.portfolio.service;

import com.stocks.diversification.portfolio.data.PortfolioDTOResponse;

public interface PortfolioReadPlatformService {
    PortfolioDTOResponse getDiversificationScore(Long userId);

    PortfolioDTOResponse getPortfolioSummary(Long userId);
}
