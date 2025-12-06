package com.stocks.service;

import com.stocks.data.StockResponse;

public interface StockReadPlatformService {
    StockResponse getStockBySymbol(String symbol);
}
