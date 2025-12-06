package com.stocks.diversification.portfolio.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.stocks.data.Stock;
import com.stocks.diversification.portfolio.data.AnalysisInsight;
import com.stocks.diversification.portfolio.data.Portfolio;
import com.stocks.diversification.portfolio.rules.PortfolioAnalysisRule;

@Service
public class PortfolioAnalyzerEngine {

    private final List<PortfolioAnalysisRule> rules;

    public PortfolioAnalyzerEngine(List<PortfolioAnalysisRule> rules) {
        this.rules = rules;
    }

    public List<AnalysisInsight> analyze(List<Portfolio> portfolios, Map<String, Stock> stockData,
            Map<Long, String> sectorMap) {
        List<AnalysisInsight> allInsights = new ArrayList<>();
        for (PortfolioAnalysisRule rule : rules) {
            allInsights.addAll(rule.evaluate(portfolios, stockData, sectorMap));
        }
        return allInsights;
    }

    public int calculateHealthScore(List<AnalysisInsight> insights) {
        int score = 100;
        for (AnalysisInsight insight : insights) {
            if (insight.getType() == AnalysisInsight.InsightType.CRITICAL) {
                score -= 20;
            } else if (insight.getType() == AnalysisInsight.InsightType.WARNING) {
                score -= 10;
            }
        }
        return Math.max(0, score);
    }
}
