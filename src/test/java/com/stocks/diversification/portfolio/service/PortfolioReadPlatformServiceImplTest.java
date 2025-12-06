package com.stocks.diversification.portfolio.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.stocks.data.Stock;
import com.stocks.diversification.portfolio.data.Portfolio;
import com.stocks.diversification.portfolio.data.PortfolioDTOResponse;
import com.stocks.diversification.portfolio.repo.PortfolioRepository;
import com.stocks.diversification.sectors.data.Sector;
import com.stocks.diversification.sectors.repo.SectorRepository;
import com.stocks.repo.StockRepository;

@ExtendWith(MockitoExtension.class)
public class PortfolioReadPlatformServiceImplTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private SectorRepository sectorRepository;

    @Mock
    private PortfolioAnalyzerEngine portfolioAnalyzerEngine;

    private PortfolioReadPlatformServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PortfolioReadPlatformServiceImpl(portfolioRepository, stockRepository, sectorRepository,
                portfolioAnalyzerEngine);
    }

    @Test
    void testGetPortfolioSummary() {
        Portfolio p1 = Portfolio.builder().stockSymbol("AAPL").quantity(10).purchasePrice(new BigDecimal("150"))
                .build();
        Portfolio p2 = Portfolio.builder().stockSymbol("GOOG").quantity(5).purchasePrice(new BigDecimal("2000"))
                .build();

        Stock s1 = Stock.builder().symbol("AAPL").price(180.0).sectorId(1L).build();
        Stock s2 = Stock.builder().symbol("GOOG").price(1900.0).sectorId(2L).build();

        Sector sec1 = Sector.builder().id(1L).name("Tech").build();
        Sector sec2 = Sector.builder().id(2L).name("Services").build();

        when(portfolioRepository.findByUserId(anyLong())).thenReturn(Arrays.asList(p1, p2));
        when(stockRepository.findBySymbolIn(anyList())).thenReturn(Arrays.asList(s1, s2));
        when(sectorRepository.findAllById(any())).thenReturn(Arrays.asList(sec1, sec2));

        PortfolioDTOResponse response = service.getPortfolioSummary(1L);

        assertEquals(new BigDecimal("11500"), response.getTotalInvestment());
        assertNotNull(response.getCurrentValue());

        assertEquals(11300.0, response.getCurrentValue().doubleValue());

        assertEquals(-200.0, response.getTotalProfitLoss().doubleValue());

        assertNotNull(response.getSectorAllocation());
        assertEquals(2, response.getSectorAllocation().size());

        assertNotNull(response.getSectorAllocation().get("Tech"));
        assertNotNull(response.getSectorAllocation().get("Services"));
    }

    @Test
    void testSmartAnalyzerInsights() {
        // Setup
        Portfolio p1 = Portfolio.builder().stockSymbol("AAPL").quantity(10).purchasePrice(new BigDecimal("150"))
                .build();
        Stock s1 = Stock.builder().symbol("AAPL").price(120.0).sectorId(1L).build(); // 20% Loss
        Sector sec1 = Sector.builder().id(1L).name("Tech").build();

        // Mocks
        when(portfolioRepository.findByUserId(anyLong())).thenReturn(java.util.Collections.singletonList(p1));
        when(stockRepository.findBySymbolIn(anyList())).thenReturn(java.util.Collections.singletonList(s1));
        when(sectorRepository.findAllById(any())).thenReturn(java.util.Collections.singletonList(sec1));

        // Mock Engine Behavior
        when(portfolioAnalyzerEngine.analyze(anyList(), any(), any())).thenReturn(
                java.util.Collections.singletonList(
                        new com.stocks.diversification.portfolio.data.AnalysisInsight(
                                com.stocks.diversification.portfolio.data.AnalysisInsight.InsightType.CRITICAL,
                                com.stocks.diversification.portfolio.data.AnalysisInsight.InsightCategory.STOCK_PERFORMANCE,
                                "Crash", "Sell")));
        when(portfolioAnalyzerEngine.calculateHealthScore(anyList())).thenReturn(80);

        // Execute
        PortfolioDTOResponse response = service.getPortfolioSummary(1L);

        // Verify
        assertEquals(1200.0, response.getCurrentValue().doubleValue());
        assertEquals(80, response.getHealthScore());
        assertEquals(1, response.getInsights().size());
        assertEquals("Crash", response.getInsights().get(0).getMessage());
    }
}
