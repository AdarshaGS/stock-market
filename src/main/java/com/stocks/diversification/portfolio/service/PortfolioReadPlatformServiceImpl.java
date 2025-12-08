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
import com.stocks.diversification.portfolio.data.MarketCapAllocation;
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
                    .recommendations(List.of("Start investing to see your portfolio analysis."))
                    .totalInvestment(BigDecimal.ZERO)
                    .currentValue(BigDecimal.ZERO)
                    .totalProfitLoss(BigDecimal.ZERO)
                    .totalProfitLossPercentage(BigDecimal.ZERO)
                    .sectorAllocation(new HashMap<>())
                    .healthScore(0)
                    .insights(new ArrayList<>())
                    .build();
        }

        // 1. Fetch Stocks and Sectors
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

        // 2. Aggregate Data
        BigDecimal totalInvestment = BigDecimal.ZERO;
        BigDecimal currentValue = BigDecimal.ZERO;
        Map<String, BigDecimal> sectorValueMap = new HashMap<>();

        // Tracks for market cap (retaining existing logic)
        BigDecimal largeCapValue = BigDecimal.ZERO;
        BigDecimal midCapValue = BigDecimal.ZERO;
        BigDecimal smallCapValue = BigDecimal.ZERO;

        for (Portfolio portfolio : userPortfolios) {
            Stock stock = stockMap.get(portfolio.getStockSymbol().toUpperCase());

            BigDecimal qty = BigDecimal.valueOf(portfolio.getQuantity());
            BigDecimal buyPrice = portfolio.getPurchasePrice();

            BigDecimal currentPrice = buyPrice; // Fallback
            if (stock != null && stock.getPrice() != null) {
                currentPrice = BigDecimal.valueOf(stock.getPrice());
            } else if (portfolio.getCurrentPrice() != null) {
                currentPrice = portfolio.getCurrentPrice();
            }

            BigDecimal investment = qty.multiply(buyPrice);
            BigDecimal curVal = qty.multiply(currentPrice);

            totalInvestment = totalInvestment.add(investment);
            currentValue = currentValue.add(curVal);

            // Sector Grouping
            String sectorName = "Unknown";
            if (stock != null && stock.getSectorId() != null) {
                sectorName = sectorNameMap.get(stock.getSectorId());
            }
            sectorValueMap.merge(sectorName, curVal, BigDecimal::add);

            // Market Cap Grouping
            if (stock != null && stock.getMarketCap() != null) {
                double mc = stock.getMarketCap();
                if (mc >= 20000) {
                    largeCapValue = largeCapValue.add(curVal);
                } else if (mc >= 5000) {
                    midCapValue = midCapValue.add(curVal);
                } else {
                    smallCapValue = smallCapValue.add(curVal);
                }
            } else {
                smallCapValue = smallCapValue.add(curVal);
            }
        }

        BigDecimal totalProfitLoss = currentValue.subtract(totalInvestment);
        BigDecimal totalProfitLossPercentage = BigDecimal.ZERO;
        if (totalInvestment.compareTo(BigDecimal.ZERO) > 0) {
            totalProfitLossPercentage = totalProfitLoss.divide(totalInvestment, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        // 3. Compute Sector Allocation Percentages
        Map<String, BigDecimal> sectorAllocationPct = new HashMap<>();
        if (currentValue.compareTo(BigDecimal.ZERO) > 0) {
            for (Map.Entry<String, BigDecimal> entry : sectorValueMap.entrySet()) {
                BigDecimal pct = entry.getValue().divide(currentValue, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                sectorAllocationPct.put(entry.getKey(), pct);
            }
        }

        // 4. Compute Diversification Score & Recommendations
        int diversificationScore = calculateDiversificationScore(sectorAllocationPct);
        String assessment = getAssessment(diversificationScore);
        List<String> recommendations = generateRecommendations(sectorAllocationPct, diversificationScore);

        // 5. Existing Analysis Engine
        List<AnalysisInsight> insights = portfolioAnalyzerEngine.analyze(userPortfolios, stockMap, sectorNameMap);
        int healthScore = portfolioAnalyzerEngine.calculateHealthScore(insights);

        // Market Cap Allocation
        MarketCapAllocation mcAllocation = new MarketCapAllocation(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        if (currentValue.compareTo(BigDecimal.ZERO) > 0) {
            mcAllocation = new MarketCapAllocation(
                    largeCapValue.divide(currentValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)),
                    midCapValue.divide(currentValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)),
                    smallCapValue.divide(currentValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)));
        }

        return PortfolioDTOResponse.builder()
                .totalInvestment(totalInvestment)
                .currentValue(currentValue)
                .totalProfitLoss(totalProfitLoss)
                .totalProfitLossPercentage(totalProfitLossPercentage)
                .sectorAllocation(sectorAllocationPct)
                .score(diversificationScore)
                .assessment(assessment)
                .recommendations(recommendations)
                .healthScore(healthScore)
                .insights(insights)
                .marketCapAllocation(mcAllocation)
                .build();
    }

    private int calculateDiversificationScore(Map<String, BigDecimal> sectorAllocation) {
        if (sectorAllocation.isEmpty())
            return 0;

        int score = 100;
        int sectorCount = sectorAllocation.size();

        // Penalize for low sector count
        if (sectorCount == 1)
            score -= 50;
        else if (sectorCount == 2)
            score -= 30;
        else if (sectorCount == 3)
            score -= 10;

        // Find max sector weight
        double maxSectorWeight = sectorAllocation.values().stream()
                .mapToDouble(BigDecimal::doubleValue)
                .max().orElse(0.0);

        // Penalize for high concentration
        if (maxSectorWeight > 70.0)
            score -= 30;
        else if (maxSectorWeight > 50.0)
            score -= 20;
        else if (maxSectorWeight > 40.0)
            score -= 10;

        return Math.max(0, Math.min(100, score));
    }

    private String getAssessment(int score) {
        if (score >= 80)
            return "Well Diversified";
        if (score >= 60)
            return "Moderately Diversified";
        if (score >= 40)
            return "Concentrated";
        return "Needs Improvement";
    }

    private List<String> generateRecommendations(Map<String, BigDecimal> sectorAllocation, int score) {
        List<String> recs = new ArrayList<>();
        int sectorCount = sectorAllocation.size();

        if (sectorCount <= 2) {
            recs.add("Consider diversifying into more sectors.");
        }

        for (Map.Entry<String, BigDecimal> entry : sectorAllocation.entrySet()) {
            if (entry.getValue().doubleValue() > 50.0) {
                recs.add(String.format("High exposure to %s sector (%.2f%%). Consider reducing.", entry.getKey(),
                        entry.getValue()));
            }
        }

        if (sectorAllocation.containsKey("Unknown") && sectorAllocation.get("Unknown").doubleValue() > 10.0) {
            recs.add(
                    "A significant portion of your portfolio is mapped to 'Unknown' sector. Consider updating sector classifications.");
        }

        if (recs.isEmpty() && score < 50) {
            recs.add("Your portfolio is concentrated. Look for opportunities in new industries.");
        } else if (recs.isEmpty()) {
            recs.add("Your portfolio looks well diversified!");
        }

        return recs;
    }
}
