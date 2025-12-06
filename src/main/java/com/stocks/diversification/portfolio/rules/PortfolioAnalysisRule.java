package com.stocks.diversification.portfolio.rules;

import java.util.List;
import java.util.Map;

import com.stocks.data.Stock;
import com.stocks.diversification.portfolio.data.AnalysisInsight;
import com.stocks.diversification.portfolio.data.Portfolio;

public interface PortfolioAnalysisRule {
    /**
     * Evaluate the portfolio and return a list of insights.
     * 
     * @param portfolios The list of portfolio items
     * @param stockData  Map of symbol to Stock entity (enrichment data)
     * @param sectorMap  Map of sector ID to Sector Name
     * @return List of insights, or empty list if no rules triggered
     */
    List<AnalysisInsight> evaluate(List<Portfolio> portfolios, Map<String, Stock> stockData,
            Map<Long, String> sectorMap);
}
