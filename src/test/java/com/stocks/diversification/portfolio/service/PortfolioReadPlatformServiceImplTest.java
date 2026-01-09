package com.stocks.diversification.portfolio.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.investments.stocks.data.Stock;
import com.investments.stocks.diversification.portfolio.data.Portfolio;
import com.investments.stocks.diversification.portfolio.data.PortfolioDTOResponse;
import com.investments.stocks.diversification.portfolio.repo.PortfolioRepository;
import com.investments.stocks.diversification.portfolio.service.PortfolioAllocationService;
import com.investments.stocks.diversification.portfolio.service.PortfolioInsightService;
import com.investments.stocks.diversification.portfolio.service.PortfolioReadPlatformServiceImpl;
import com.investments.stocks.diversification.portfolio.service.PortfolioRiskEvaluationService;
import com.investments.stocks.diversification.portfolio.service.PortfolioScoringService;
import com.investments.stocks.diversification.portfolio.service.PortfolioValuationService;
import com.investments.stocks.diversification.sectors.data.Sector;
import com.investments.stocks.diversification.sectors.repo.SectorRepository;
import com.investments.stocks.repo.StockRepository;

@ExtendWith(MockitoExtension.class)
public class PortfolioReadPlatformServiceImplTest {

        @Mock
        private PortfolioRepository portfolioRepository;

        @Mock
        private StockRepository stockRepository;

        @Mock
        private SectorRepository sectorRepository;

        private PortfolioReadPlatformServiceImpl service;

        @BeforeEach
        void setUp() {
                // Use real services for logic testing, mock repositories for data
                PortfolioValuationService valuationService = new PortfolioValuationService();
                PortfolioAllocationService allocationService = new PortfolioAllocationService();
                PortfolioRiskEvaluationService riskService = new PortfolioRiskEvaluationService();
                PortfolioScoringService scoringService = new PortfolioScoringService();
                PortfolioInsightService insightService = new PortfolioInsightService();

                service = new PortfolioReadPlatformServiceImpl(portfolioRepository, stockRepository, sectorRepository,
                                valuationService, allocationService, riskService, scoringService, insightService);
        }

        @Test
        void testGetPortfolioSummary_WellDiversified() {
                // Setup: 3 Different Stocks, 3 Different Sectors, Large Cap
                Portfolio p1 = Portfolio.builder().stockSymbol("A").quantity(10).purchasePrice(new BigDecimal("100"))
                                .build();
                Portfolio p2 = Portfolio.builder().stockSymbol("B").quantity(10).purchasePrice(new BigDecimal("100"))
                                .build();
                Portfolio p3 = Portfolio.builder().stockSymbol("C").quantity(10).purchasePrice(new BigDecimal("100"))
                                .build();

                // Large Cap (>20000) to avoid Small Cap penalty
                Stock s1 = Stock.builder().symbol("A").price(100.0).sectorId(1L).marketCap(25000.0).build();
                Stock s2 = Stock.builder().symbol("B").price(100.0).sectorId(2L).marketCap(25000.0).build();
                Stock s3 = Stock.builder().symbol("C").price(100.0).sectorId(3L).marketCap(25000.0).build();

                Sector sec1 = Sector.builder().id(1L).name("Tech").build();
                Sector sec2 = Sector.builder().id(2L).name("Finance").build();
                Sector sec3 = Sector.builder().id(3L).name("Energy").build();

                when(portfolioRepository.findByUserId(anyLong())).thenReturn(Arrays.asList(p1, p2, p3));
                when(stockRepository.findBySymbolIn(anyList())).thenReturn(Arrays.asList(s1, s2, s3));
                when(sectorRepository.findAllById(any())).thenReturn(Arrays.asList(sec1, sec2, sec3));

                // Execute
                PortfolioDTOResponse response = service.getPortfolioSummary(1L);

                // Verify
                // Total Value = 3000
                assertEquals(new BigDecimal("3000.0"), response.getCurrentValue());

                // Analysis:
                // Max Stock = 33.3% -> No Penalty (>40 is -35, >25 is -20). But wait, 33.3 >
                // 25.
                // So Penalty: -20 (Single Stock > 25%).
                // Max Sector = 33.3% -> No Penalty (>40 is -15). (Warning > 30? Yes).
                // Small Cap: 0% -> No Penalty.
                // Drawdown: 0 -> No Penalty.
                // Warnings: Stock > 10% (3 warnings), Sector > 30% (3 warnings). Total 6
                // warnings.
                // Warnings > 3 -> Penalty -10.

                // Total Penalty = -20 (Stock>25) -10 (Warnings>3) = -30.
                // Score = 70.
                // Assessment: 65-79 -> MODERATELY_DIVERSIFIED.

                // Wait, logic check:
                // Stock 1: 33% -> >25% Risk (Critical/Penalty -20). Also >10% Warning.
                // Stock 2: 33% -> >25% Risk.
                // Stock 3: 33% -> >25% Risk.
                // Does "Single stock > 25%" penalty apply once or multiple times?
                // My implementation: if (maxStockPct > 25) score -= 20. Once.
                // Warnings:
                // S1 > 10% -> Warning?
                // My implementation:
                // if > 40 -> Critical
                // else if > 25 -> Critical (Risk desc, but InsightType.Critical?)
                // implementation: if > 25 -> INTRODUCES InsightType.CRITICAL.
                // if > 10 -> INTRODUCES InsightType.WARNING.
                // Current code: if/else if/else if.
                // if (pct > 40) ... else if (pct > 25) ... else if (pct > 10) ...
                // So > 25 generates CRITICAL. Not WARNING.
                // So 3 Criticals. 0 Warnings from stocks.

                // Sectors: 33% each.
                // if > 40 ... else if > 30 ...
                // 33 > 30 -> 3 Warnings.

                // Total Warnings = 3.
                // "More than 3 warnings" -> 3 is not > 3. No penalty.

                // So Score = 100 - 20 (Stock>25) = 80.
                // Assessment: 80-90 -> STRONG_AND_BALANCED.

                assertEquals(80, response.getScore());
                assertEquals("STRONG_AND_BALANCED", response.getAssessment());
        }

        @Test
        void testGetPortfolioSummary_Concentrated_OneStock_SmallCap() {
                // 1 Stock -> 100% Allocation, Small Cap
                Portfolio p1 = Portfolio.builder().stockSymbol("A").quantity(10).purchasePrice(new BigDecimal("100"))
                                .build();
                // Small Cap (<5000 or null)
                Stock s1 = Stock.builder().symbol("A").price(100.0).sectorId(1L).marketCap(1000.0).build();
                Sector sec1 = Sector.builder().id(1L).name("Tech").build();

                when(portfolioRepository.findByUserId(anyLong())).thenReturn(Arrays.asList(p1));
                when(stockRepository.findBySymbolIn(anyList())).thenReturn(Arrays.asList(s1));
                when(sectorRepository.findAllById(any())).thenReturn(Arrays.asList(sec1));

                PortfolioDTOResponse response = service.getPortfolioSummary(1L);

                // Score Analysis:
                // 1. Single Stock 100% (>40%) -> -35.
                // 2. Single Sector 100% (>40%) -> -15.
                // 3. Small Cap 100% (>70%) -> -15.
                // 4. Drawdown 0 -> 0.
                // 5. Warnings?
                // Stock Insight: Critical (>40).
                // Sector Insight: Critical (>40).
                // Small Cap Insight: Critical (>70).
                // Warnings list is empty (all critical).
                // "More than 3 warnings" -> No penalty.

                // Total Penalty = 35 + 15 + 15 = 65.
                // Score = 100 - 65 = 35.

                assertEquals(35, response.getScore());
                assertEquals("POORLY_DIVERSIFIED", response.getAssessment());

                // Check Insights
                assertTrue(response.getInsights().getCritical().stream()
                                .anyMatch(i -> i.getMessage().contains("Critical exposure to single stock")));
                assertTrue(response.getInsights().getCritical().stream()
                                .anyMatch(i -> i.getMessage().contains("High Small-Cap Exposure")));
        }

        @Test
        void testGetPortfolioSummary_StockCrash() {
                // 1 Stock, Large Cap, Crashed 50%
                Portfolio p1 = Portfolio.builder().stockSymbol("A").quantity(10).purchasePrice(new BigDecimal("200"))
                                .build();
                Stock s1 = Stock.builder().symbol("A").price(100.0).sectorId(1L).marketCap(50000.0).build();
                Sector sec1 = Sector.builder().id(1L).name("Tech").build();

                when(portfolioRepository.findByUserId(anyLong())).thenReturn(Collections.singletonList(p1));
                when(stockRepository.findBySymbolIn(anyList())).thenReturn(Collections.singletonList(s1));
                when(sectorRepository.findAllById(any())).thenReturn(Collections.singletonList(sec1));

                PortfolioDTOResponse response = service.getPortfolioSummary(1L);

                // Score:
                // Stock > 40% (100%) -> -35
                // Sector > 40% (100%) -> -15
                // Small Cap (0%) -> 0
                // Drawdown (Current 1000, Buy 2000 -> -50%). < -25% -> -10.
                // Warnings: 0 (Criticals only).

                // Total Penalty = 35 + 15 + 10 = 60.
                // Score = 40.
                // Assessment: POORLY_DIVERSIFIED (<45).

                assertEquals(40, response.getScore());
                assertTrue(response.getInsights().getCritical().stream()
                                .anyMatch(i -> i.getMessage().contains("Stock crash alert")));
        }
}
