package com.stocks.service;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.stocks.data.Stock;
import com.stocks.data.StockResponse;
import com.stocks.diversification.sectors.data.Sector;
import com.stocks.diversification.sectors.repo.SectorRepository;
import com.stocks.diversification.sectors.service.SectorNormalizer;
import com.stocks.exception.SymbolNotFoundException;
import com.stocks.repo.StockRepository;
import com.stocks.thirdParty.ThirdPartyResponse;
import com.stocks.thirdParty.service.IndianAPIService;

@Service
public class StockReadPlatformServiceImpl implements StockReadPlatformService {

    final JdbcTemplate jdbcTemplate;
    final IndianAPIService indianAPIService;
    final StockRepository stockRepository;
    final SectorRepository sectorRepository;
    final SectorNormalizer sectorNormalizer;

    public StockReadPlatformServiceImpl(final JdbcTemplate jdbcTemplate, final IndianAPIService indianAPIService,
            final StockRepository stockRepository, final SectorRepository sectorRepository,
            final SectorNormalizer sectorNormalizer) {
        this.jdbcTemplate = jdbcTemplate;
        this.indianAPIService = indianAPIService;
        this.stockRepository = stockRepository;
        this.sectorRepository = sectorRepository;
        this.sectorNormalizer = sectorNormalizer;
    }

    @Override
    public StockResponse getStockBySymbol(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return null;
        }
        Stock stock = null;
        StockResponse stockResponse = null;
        symbol = symbol.toUpperCase();
        String sql = "SELECT * FROM stocks WHERE symbol = ?";

        try {
            stock = jdbcTemplate.queryForObject(
                    sql,
                    new BeanPropertyRowMapper<>(Stock.class),
                    symbol);
            stockResponse = stockResponseBuilder(stock);
        } catch (EmptyResultDataAccessException ex) {
            stockResponse = fetchFromThirdParty(symbol, stock);
        }
        return stockResponse;
    }

    public StockResponse fetchFromThirdParty(String symbol, Stock stock) {
        ThirdPartyResponse response = this.indianAPIService.fetchStockData(symbol);
        if (response == null) {
            throw new SymbolNotFoundException("Symbol not found in third-party API: " + symbol);
        }

        String rawIndustry = response.getCompanyProfile().getMgIndustry();
        String normalizedSectorName = sectorNormalizer.normalize(rawIndustry);

        // Check if Normalized Sector Name exists in DB
        Long sectorId = this.sectorRepository.findIdByName(normalizedSectorName);

        if (sectorId == null) {
            // If not found (e.g. first time seeing "Technology"), create it
            Sector sector = Sector.builder().name(normalizedSectorName).build();
            this.sectorRepository.save(sector);
            sectorId = sector.getId();
        }

        stock = Stock.builder()
                .symbol(symbol)
                .companyName(response.getCompanyName())
                .description(response.getCompanyProfile().getCompanyDescription())
                .price(response.getCurrentPrice().getNSE())
                .sectorId(sectorId)
                .build();
        stock = this.stockRepository.save(stock);
        return stockResponseBuilder(stock);
    }

    public StockResponse stockResponseBuilder(Stock stock) {
        Sector sector = this.sectorRepository.findById(stock.getSectorId()).orElse(null);
        return StockResponse.builder()
                .companyName(stock.getCompanyName())
                .description(stock.getDescription())
                .price(stock.getPrice())
                .sector(sector != null ? sector.getName() : null)
                .build();
    }

}
