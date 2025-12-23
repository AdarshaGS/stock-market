package com.investments.stocks.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.investments.stocks.data.Stock;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    List<Stock> findBySymbolIn(List<String> symbols);

    Stock findBySymbol(String symbol);

    Long findIdBySymbol(String symbol);
}
