package com.stocks.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stocks.data.StockResponse;
import com.stocks.service.StockReadPlatformService;

@RestController
@RequestMapping("/api/stocks")
public class StockApiResource {


    private final StockReadPlatformService stockReadPlatformService;

    public StockApiResource(final StockReadPlatformService stockReadPlatformService) {
        this.stockReadPlatformService = stockReadPlatformService;
    }
    

    @GetMapping("/{symbol}")
    public StockResponse getStockBySymbol(@PathVariable String symbol) {
        return this.stockReadPlatformService.getStockBySymbol(symbol);
    }
}
