package com.stocks.diversification.portfolio.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.stocks.data.Stock;
import com.stocks.diversification.portfolio.data.Portfolio;
import com.stocks.diversification.portfolio.data.PortfolioDTOResponse;
import com.stocks.diversification.portfolio.repo.PortfolioRepository;
import com.stocks.diversification.sectors.data.Sector;
import com.stocks.diversification.sectors.repo.SectorRepository;
import com.stocks.repo.StockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioReadPlatformServiceImpl implements PortfolioReadPlatformService {

    private final PortfolioRepository portfolioRepository;
    private final StockRepository stockRepository;
    private final SectorRepository sectorRepository;

    @Override
    public PortfolioDTOResponse getDiversificationScore(Long userId) {
        // TODO: Implement diversification score logic
        return new PortfolioDTOResponse();
    }

    @Override
    public PortfolioDTOResponse getPortfolioSummary(Long userId) {
        List<Portfolio> userPortfolios = portfolioRepository.findByUserId(userId);

        if (userPortfolios.isEmpty()) {
            return PortfolioDTOResponse.builder()
                    .totalInvestment(BigDecimal.ZERO)
                    .currentValue(BigDecimal.ZERO)
                    .totalProfitLoss(BigDecimal.ZERO)
                    .sectorAllocation(new HashMap<>())
                    .build();
        }

        List<String> symbols = userPortfolios.stream()
                .map(Portfolio::getStockSymbol)
                .collect(Collectors.toList());

        List<Stock> stocks = stockRepository.findBySymbolIn(symbols);
        Map<String, Stock> stockMap = stocks.stream()
                .collect(Collectors.toMap(Stock::getSymbol, Function.identity()));

        List<Long> sectorIds = stocks.stream()
                .map(Stock::getSectorId)
                .distinct()
                .collect(Collectors.toList());

        List<Sector> sectors = sectorRepository.findAllById(sectorIds);
        Map<Long, String> sectorMap = sectors.stream()
                .collect(Collectors.toMap(Sector::getId, Sector::getName));

        BigDecimal totalInvestment = BigDecimal.ZERO;
        BigDecimal currentValue = BigDecimal.ZERO;
        Map<String, BigDecimal> sectorAllocation = new HashMap<>();

        for (Portfolio p : userPortfolios) {
            BigDecimal investment = p.getPurchasePrice().multiply(BigDecimal.valueOf(p.getQuantity()));
            totalInvestment = totalInvestment.add(investment);

            Stock stock = stockMap.get(p.getStockSymbol());
            if (stock != null) {
                BigDecimal currentPrice = BigDecimal.valueOf(stock.getPrice());
                BigDecimal currentVal = currentPrice.multiply(BigDecimal.valueOf(p.getQuantity()));
                currentValue = currentValue.add(currentVal);

                String sectorName = sectorMap.get(stock.getSectorId());
                if (sectorName != null) {
                    sectorAllocation.merge(sectorName, currentVal, BigDecimal::add);
                }
            }
        }

        BigDecimal totalProfitLoss = currentValue.subtract(totalInvestment);

        return PortfolioDTOResponse.builder()
                .totalInvestment(totalInvestment)
                .currentValue(currentValue)
                .totalProfitLoss(totalProfitLoss)
                .sectorAllocation(sectorAllocation)
                .build();
    }

}
