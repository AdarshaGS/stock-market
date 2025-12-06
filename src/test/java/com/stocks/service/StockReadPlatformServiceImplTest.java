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
import com.stocks.thirdParty.service.IndianAPIService;

public class StockReadPlatformServiceImplTest {

    private JdbcTemplate jdbcTemplate;
    private IndianAPIService indianAPIService;
    private StockRepository stockRepository;
    private SectorRepository sectorRepository;
    private SectorNormalizer sectorNormalizer;
    private StockReadPlatformServiceImpl service;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        indianAPIService = mock(IndianAPIService.class);
        stockRepository = mock(StockRepository.class);
        sectorRepository = mock(SectorRepository.class);
        sectorNormalizer = new SectorNormalizer(); // Use real normalizer to test logic
        service = new StockReadPlatformServiceImpl(jdbcTemplate, indianAPIService, stockRepository, sectorRepository,
                sectorNormalizer);
    }

    @Test
    void testFetchFromThirdParty_NormalizesSector() {
        // Setup
        String symbol = "INFY";
        ThirdPartyResponse mockResponse = new ThirdPartyResponse();
        mockResponse.setCompanyName("Infosys");
        ThirdPartyResponse.CompanyProfile profile = new ThirdPartyResponse.CompanyProfile();
        profile.setMgIndustry("Computers - Software"); // Raw Industry
        mockResponse.setCompanyProfile(profile);
        ThirdPartyResponse.CurrentPrice price = new ThirdPartyResponse.CurrentPrice();
        price.setNSE(1500.0);
        mockResponse.setCurrentPrice(price);

        when(indianAPIService.fetchStockData(symbol)).thenReturn(mockResponse);
        when(sectorRepository.findIdByName("Computers - Software")).thenReturn(10L);
        when(stockRepository.save(any(Stock.class))).thenAnswer(i -> i.getArguments()[0]);
        when(sectorRepository.findById(10L))
                .thenReturn(java.util.Optional.of(Sector.builder().id(10L).name("Computers - Software").build()));

        // Execute
        StockResponse response = service.fetchFromThirdParty(symbol, null);

        // Verify
        assertNotNull(response);
        assertEquals("Computers - Software", response.getSector());
        verify(sectorRepository).findIdByName("Computers - Software");
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

        when(indianAPIService.fetchStockData(symbol)).thenReturn(mockResponse);
        when(sectorRepository.findIdByName("Banks - Private Sector")).thenReturn(20L);
        when(stockRepository.save(any(Stock.class))).thenAnswer(i -> i.getArguments()[0]);
        when(sectorRepository.findById(20L))
                .thenReturn(java.util.Optional.of(Sector.builder().id(20L).name("Banks - Private Sector").build()));

        // Execute
        StockResponse response = service.fetchFromThirdParty(symbol, null);

        // Verify
        assertEquals("Banks - Private Sector", response.getSector());
    }
}
