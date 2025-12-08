package com.stocks.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import com.stocks.data.Stock;
import com.stocks.data.StockResponse;
import com.stocks.diversification.sectors.data.Sector;
import com.stocks.diversification.sectors.repo.SectorRepository;
import com.stocks.diversification.sectors.service.SectorNormalizer;
import com.stocks.repo.StockRepository;
import com.stocks.thirdParty.ThirdPartyResponse;
import com.stocks.thirdParty.providers.IndianAPI.service.IndianAPIService;
import com.stocks.thirdParty.factory.StockDataProviderFactory;

public class StockReadPlatformServiceImplTest {

        private JdbcTemplate jdbcTemplate;
        private IndianAPIService indianAPIService;
        private StockRepository stockRepository;
        private SectorRepository sectorRepository;
        private SectorNormalizer sectorNormalizer;
        private StockReadPlatformServiceImpl service;
        private StockDataProviderFactory stockDataProviderFactory;

        @BeforeEach
        void setUp() {
                jdbcTemplate = mock(JdbcTemplate.class);
                indianAPIService = mock(IndianAPIService.class);
                stockRepository = mock(StockRepository.class);
                sectorRepository = mock(SectorRepository.class);
                sectorNormalizer = new SectorNormalizer();
                stockDataProviderFactory = mock(StockDataProviderFactory.class);

                service = new StockReadPlatformServiceImpl(jdbcTemplate, indianAPIService, stockRepository,
                                sectorRepository,
                                sectorNormalizer, stockDataProviderFactory);
        }

        @Test
        void testFetchFromThirdParty_NormalizesSector() {
                // Setup
                String symbol = "INFY";
                ThirdPartyResponse mockResponse = new ThirdPartyResponse();
                mockResponse.setCompanyName("Infosys");
                ThirdPartyResponse.CompanyProfile profile = new ThirdPartyResponse.CompanyProfile();
                profile.setMgIndustry("Computers - Software");
                mockResponse.setCompanyProfile(profile);
                ThirdPartyResponse.CurrentPrice price = new ThirdPartyResponse.CurrentPrice();
                price.setNSE(1500.0);
                mockResponse.setCurrentPrice(price);

                // Mock Market Cap Data
                // ThirdPartyResponse.StockDetailsReusableData stockDetails = ThirdPartyResponse.StockDetailsReusableData
                //                 .builder()
                //                 .marketCap(500000.0).build();
                // mockResponse.setStockDetailsReusableData(stockDetails);

                when(stockDataProviderFactory.fetchStockDataWithRetry(symbol)).thenReturn(mockResponse);
                when(sectorRepository.findIdByName("Information Technology")).thenReturn(10L);
                when(stockRepository.save(any(Stock.class))).thenAnswer(i -> i.getArguments()[0]);
                when(sectorRepository.findById(10L))
                                .thenReturn(java.util.Optional
                                                .of(Sector.builder().id(10L).name("Information Technology").build()));

                // Execute
                StockResponse response = service.fetchFromThirdParty(symbol, null);

                // Verify
                assertNotNull(response);
                assertEquals("Information Technology", response.getSector());
                verify(sectorRepository).findIdByName("Information Technology");
        }

        @Test
        void testFetchFromThirdParty_NormalizesBanking() {
                // Setup
                String symbol = "HDFCBANK";
                ThirdPartyResponse mockResponse = new ThirdPartyResponse();
                mockResponse.setCompanyName("HDFC Bank");
                ThirdPartyResponse.CompanyProfile profile = new ThirdPartyResponse.CompanyProfile();
                profile.setMgIndustry("Banks - Private Sector");
                mockResponse.setCompanyProfile(profile);
                ThirdPartyResponse.CurrentPrice price = new ThirdPartyResponse.CurrentPrice();
                price.setNSE(1600.0);
                mockResponse.setCurrentPrice(price);

                // Mock Market Cap Data
                // ThirdPartyResponse.StockDetailsReusableData stockDetails = ThirdPartyResponse.StockDetailsReusableData
                //                 .builder()
                //                 .marketCap(800000.0).build();
                // mockResponse.setStockDetailsReusableData(stockDetails);

                when(stockDataProviderFactory.fetchStockDataWithRetry(symbol)).thenReturn(mockResponse);
                when(sectorRepository.findIdByName("Financials")).thenReturn(20L);
                when(stockRepository.save(any(Stock.class))).thenAnswer(i -> i.getArguments()[0]);
                when(sectorRepository.findById(20L))
                                .thenReturn(java.util.Optional.of(Sector.builder().id(20L).name("Financials").build()));

                // Execute
                StockResponse response = service.fetchFromThirdParty(symbol, null);

                // Verify
                assertEquals("Financials", response.getSector());
        }
}
