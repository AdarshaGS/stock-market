package com.stocks.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stocks.data.StockResponse;
import com.stocks.service.StockReadPlatformService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/stocks")
@Tag(name = "Stock Management", description = "APIs for fetching stock details")
public class StockApiResource {

    private final StockReadPlatformService stockReadPlatformService;

    public StockApiResource(final StockReadPlatformService stockReadPlatformService) {
        this.stockReadPlatformService = stockReadPlatformService;
    }

    @GetMapping("/{symbol}")
    @Operation(summary = "Get stock by symbol", description = "Fetches stock details including price and sector.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved stock details")
    @ApiResponse(responseCode = "404", description = "Stock symbol not found")
    public StockResponse getStockBySymbol(@PathVariable String symbol) {
        return this.stockReadPlatformService.getStockBySymbol(symbol);
    }
}
