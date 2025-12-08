package com.stocks.diversification.portfolio.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        void testGetPortfolioSummary_WellDiversified() {
                // Setup: 3 Sectors, balanced
                Portfolio p1 = Portfolio.builder().stockSymbol("A").quantity(10).purchasePrice(new BigDecimal("100"))
                                .build();
                Portfolio p2 = Portfolio.builder().stockSymbol("B").quantity(10).purchasePrice(new BigDecimal("100"))
                                .build();
                Portfolio p3 = Portfolio.builder().stockSymbol("C").quantity(10).purchasePrice(new BigDecimal("100"))
                                .build();

                Stock s1 = Stock.builder().symbol("A").price(100.0).sectorId(1L).build(); // 1000
                Stock s2 = Stock.builder().symbol("B").price(100.0).sectorId(2L).build(); // 1000
                Stock s3 = Stock.builder().symbol("C").price(100.0).sectorId(3L).build(); // 1000

                Sector sec1 = Sector.builder().id(1L).name("Tech").build();
                Sector sec2 = Sector.builder().id(2L).name("Finance").build();
                Sector sec3 = Sector.builder().id(3L).name("Energy").build();

                when(portfolioRepository.findByUserId(anyLong())).thenReturn(Arrays.asList(p1, p2, p3));
                when(stockRepository.findBySymbolIn(anyList())).thenReturn(Arrays.asList(s1, s2, s3));
                when(sectorRepository.findAllById(any())).thenReturn(Arrays.asList(sec1, sec2, sec3));

                // Execute
                PortfolioDTOResponse response = service.getPortfolioSummary(1L);

                // Verify
                assertEquals(new BigDecimal("3000.0"), response.getCurrentValue());

                // Score:
                // Sector Count = 3 -> -10
                // Max Weight = 33.3% -> No Penalty
                // Score = 100 - 10 = 90
                assertEquals(90, response.getScore());
                assertEquals("Well Diversified", response.getAssessment());
                assertEquals("Your portfolio looks well diversified!", response.getRecommendations().get(0));
        }

        @Test
        void testGetPortfolioSummary_Concentrated_OneSector() {
                // 1 Sector
                Portfolio p1 = Portfolio.builder().stockSymbol("A").quantity(10).purchasePrice(new BigDecimal("100"))
                                .build();
                Stock s1 = Stock.builder().symbol("A").price(100.0).sectorId(1L).build();
                Sector sec1 = Sector.builder().id(1L).name("Tech").build();

                when(portfolioRepository.findByUserId(anyLong())).thenReturn(Arrays.asList(p1));
                when(stockRepository.findBySymbolIn(anyList())).thenReturn(Arrays.asList(s1));
                when(sectorRepository.findAllById(any())).thenReturn(Arrays.asList(sec1));

                PortfolioDTOResponse response = service.getPortfolioSummary(1L);

                // Score:
                // Sector Count = 1 -> -50
                // Max Weight = 100% -> >70% -> -30
                // Score = 100 - 50 - 30 = 20
                assertEquals(20, response.getScore());
                assertEquals("Needs Improvement", response.getAssessment());
                assertTrue(response.getRecommendations().contains("Consider diversifying into more sectors."));
                // Check for high exposure message
                boolean hasExposureMsg = response.getRecommendations().stream()
                                .anyMatch(r -> r.contains("High exposure to Tech"));
                assertTrue(hasExposureMsg);
        }

        @Test
        void testGetPortfolioSummary_TwoSectors_Balanced() {
                // 2 Sectors, 50-50
                Portfolio p1 = Portfolio.builder().stockSymbol("A").quantity(10).purchasePrice(new BigDecimal("100"))
                                .build();
                Portfolio p2 = Portfolio.builder().stockSymbol("B").quantity(10).purchasePrice(new BigDecimal("100"))
                                .build();

                Stock s1 = Stock.builder().symbol("A").price(100.0).sectorId(1L).build();
                Stock s2 = Stock.builder().symbol("B").price(100.0).sectorId(2L).build();

                Sector sec1 = Sector.builder().id(1L).name("Tech").build();
                Sector sec2 = Sector.builder().id(2L).name("Finance").build();

                when(portfolioRepository.findByUserId(anyLong())).thenReturn(Arrays.asList(p1, p2));
                when(stockRepository.findBySymbolIn(anyList())).thenReturn(Arrays.asList(s1, s2));
                when(sectorRepository.findAllById(any())).thenReturn(Arrays.asList(sec1, sec2));

                PortfolioDTOResponse response = service.getPortfolioSummary(1L);

                // Score:
                // Sector Count = 2 -> -30
                // Max Weight = 50% -> No Penalty (Penalty is > 50%) -> Wait, 50% > 40% penalty
                // is -10
                // Score = 100 - 30 - 10 = 60
                assertEquals(60, response.getScore());
                assertEquals("Moderately Diversified", response.getAssessment());
                assertTrue(response.getRecommendations().contains("Consider diversifying into more sectors."));
        }

        @Test
        void testSmartAnalyzerInsights_Integration() {
                // Setup
                Portfolio p1 = Portfolio.builder().stockSymbol("AAPL").quantity(10).purchasePrice(new BigDecimal("150"))
                                .build();
                Stock s1 = Stock.builder().symbol("AAPL").price(120.0).sectorId(1L).build();
                Sector sec1 = Sector.builder().id(1L).name("Tech").build();

                when(portfolioRepository.findByUserId(anyLong())).thenReturn(java.util.Collections.singletonList(p1));
                when(stockRepository.findBySymbolIn(anyList())).thenReturn(java.util.Collections.singletonList(s1));
                when(sectorRepository.findAllById(any())).thenReturn(java.util.Collections.singletonList(sec1));

                when(portfolioAnalyzerEngine.analyze(anyList(), any(), any())).thenReturn(
                                java.util.Collections.singletonList(
                                                new com.stocks.diversification.portfolio.data.AnalysisInsight(
                                                                com.stocks.diversification.portfolio.data.AnalysisInsight.InsightType.CRITICAL,
                                                                com.stocks.diversification.portfolio.data.AnalysisInsight.InsightCategory.STOCK_PERFORMANCE,
                                                                "Crash", "Sell")));
                when(portfolioAnalyzerEngine.calculateHealthScore(anyList())).thenReturn(80);

                PortfolioDTOResponse response = service.getPortfolioSummary(1L);

                assertEquals(80, response.getHealthScore());
                assertEquals(1, response.getInsights().size());
        }
}
