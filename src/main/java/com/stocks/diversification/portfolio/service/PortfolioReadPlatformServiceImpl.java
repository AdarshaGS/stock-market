package com.stocks.diversification.portfolio.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.stocks.data.Stock;
import com.stocks.diversification.portfolio.data.AnalysisInsight;
import com.stocks.diversification.portfolio.data.Portfolio;
import com.stocks.diversification.portfolio.data.PortfolioDTOResponse;
import com.stocks.diversification.portfolio.repo.PortfolioRepository;
import com.stocks.diversification.sectors.data.Sector;
import com.stocks.diversification.sectors.repo.SectorRepository;
import com.stocks.repo.StockRepository;

@Service
public class PortfolioReadPlatformServiceImpl implements PortfolioReadPlatformService {

    private final PortfolioRepository portfolioRepository;
    private final StockRepository stockRepository;
    private final SectorRepository sectorRepository;
    private final PortfolioAnalyzerEngine portfolioAnalyzerEngine;

    public PortfolioReadPlatformServiceImpl(PortfolioRepository portfolioRepository,
            StockRepository stockRepository,
            SectorRepository sectorRepository,
            PortfolioAnalyzerEngine portfolioAnalyzerEngine) {
        this.portfolioRepository = portfolioRepository;
        this.stockRepository = stockRepository;
        this.sectorRepository = sectorRepository;
        this.portfolioAnalyzerEngine = portfolioAnalyzerEngine;
    }

    @Override
    public PortfolioDTOResponse getPortfolioSummary(Long userId) {
        return getDiversificationScore(userId);
    }

    @Override
    public PortfolioDTOResponse getDiversificationScore(Long userId) {
        List<Portfolio> userPortfolios = portfolioRepository.findByUserId(userId);
        return generatePortfolioResponse(userPortfolios);
    }

    private PortfolioDTOResponse generatePortfolioResponse(List<Portfolio> userPortfolios) {
        if (userPortfolios.isEmpty()) {
            return PortfolioDTOResponse.builder()
                    .score(0)
                    .assessment("No Data")
                    .recommendations(List.of("Consider adding more diverse assets"))
                    .totalInvestment(BigDecimal.ZERO)
                    .currentValue(BigDecimal.ZERO)
                    .totalProfitLoss(BigDecimal.ZERO)
                    .totalProfitLossPercentage(BigDecimal.ZERO)
                    .sectorAllocation(new HashMap<>())
                    .healthScore(0)
                    .insights(new ArrayList<>())
                    .build();
        }

        List<String> stockSymbols = userPortfolios.stream()
                .map(Portfolio::getStockSymbol)
                .distinct()
                .collect(Collectors.toList());

        Map<String, Stock> stockMap = stockRepository.findBySymbolIn(stockSymbols).stream()
                .collect(Collectors.toMap(Stock::getSymbol, Function.identity()));

        Set<Long> sectorIds = stockMap.values().stream()
                .map(Stock::getSectorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> sectorNameMap = sectorRepository.findAllById(sectorIds).stream()
                .collect(Collectors.toMap(Sector::getId, Sector::getName));

        BigDecimal totalInvestment = BigDecimal.ZERO;
        BigDecimal currentValue = BigDecimal.ZERO;
        Map<String, BigDecimal> sectorValueMap = new HashMap<>();

        for (Portfolio p : userPortfolios) {
            BigDecimal qty = new BigDecimal(p.getQuantity());
            BigDecimal buyPrice = p.getPurchasePrice();

            Stock stock = stockMap.get(p.getStockSymbol());
            // Safe unwrap of current price
            BigDecimal currentItemPrice = p.getCurrentPrice();
            if (currentItemPrice == null) {
                currentItemPrice = (stock != null && stock.getPrice() != null)
                        ? BigDecimal.valueOf(stock.getPrice())
                        : buyPrice;
            }

            BigDecimal investment = qty.multiply(buyPrice);
            BigDecimal curValue = qty.multiply(currentItemPrice);

            totalInvestment = totalInvestment.add(investment);
            currentValue = currentValue.add(curValue);

            String sectorName = "Others";
            if (stock != null && stock.getSectorId() != null) {
                sectorName = sectorNameMap.getOrDefault(stock.getSectorId(), "Others");
            }
            sectorValueMap.merge(sectorName, curValue, BigDecimal::add);
        }

        BigDecimal totalProfitLoss = currentValue.subtract(totalInvestment);
        BigDecimal totalProfitLossPercentage = (totalInvestment.compareTo(BigDecimal.ZERO) > 0)
                ? totalProfitLoss.divide(totalInvestment, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        Map<String, BigDecimal> sectorAllocation = new HashMap<>();
        if (currentValue.compareTo(BigDecimal.ZERO) > 0) {
            for (Map.Entry<String, BigDecimal> entry : sectorValueMap.entrySet()) {
                BigDecimal pct = entry.getValue().divide(currentValue, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                sectorAllocation.put(entry.getKey(), pct);
            }
        }

        // Use Rule Engine
        List<AnalysisInsight> insights = portfolioAnalyzerEngine
                .analyze(userPortfolios, stockMap, sectorNameMap);
        int healthScore = portfolioAnalyzerEngine.calculateHealthScore(insights);

        // Legacy scores (can be phased out or kept as distinct metric)
        int score = calculateScore(sectorAllocation);
        String assessment = assessScore(score);
        List<String> recommendations = generateRecommendations(sectorAllocation, score);

        return PortfolioDTOResponse.builder()
                .score(score)
                .assessment(assessment)
                .recommendations(recommendations)
                .totalInvestment(totalInvestment)
                .currentValue(currentValue)
                .totalProfitLoss(totalProfitLoss)
                .totalProfitLossPercentage(totalProfitLossPercentage)
                .sectorAllocation(sectorAllocation)
                .healthScore(healthScore)
                .insights(insights)
                .build();
    }

    private int calculateScore(Map<String, BigDecimal> sectorAllocation) {
        if (sectorAllocation.isEmpty())
            return 0;
        int score = 100;
        int sectorCount = sectorAllocation.size();
        if (sectorCount < 3)
            score -= 30;
        if (sectorCount < 5)
            score -= 10;
        for (BigDecimal pct : sectorAllocation.values()) {
            if (pct.doubleValue() > 25.0)
                score -= 10;
            if (pct.doubleValue() > 50.0)
                score -= 20;
        }
        return Math.max(0, score);
    }

    private String assessScore(int score) {
        if (score >= 80)
            return "Excellent";
        if (score >= 60)
            return "Good";
        if (score >= 40)
            return "Fair";
        return "Needs Improvement";
    }

    private List<String> generateRecommendations(Map<String, BigDecimal> sectorAllocation, int score) {
        List<String> recs = new ArrayList<>();
        if (score < 60)
            recs.add("Consider diversifying into more sectors.");
        for (Map.Entry<String, BigDecimal> entry : sectorAllocation.entrySet()) {
            if (entry.getValue().doubleValue() > 30.0) {
                recs.add("High exposure to " + entry.getKey() + " sector (" + entry.getValue()
                        + "%). Consider reducing.");
            }
        }
        if (sectorAllocation.size() < 3)
            recs.add("You have investments in few sectors. Look for opportunities in new industries.");
        if (recs.isEmpty())
            recs.add("Your portfolio looks well diversified!");
        return recs;
    }
}
