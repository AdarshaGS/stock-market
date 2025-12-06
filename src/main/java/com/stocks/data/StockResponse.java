package com.stocks.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class StockResponse {
    private String companyName;
    private String description;
    private double price;
    private String sector;

    public StockResponse() {}

}
