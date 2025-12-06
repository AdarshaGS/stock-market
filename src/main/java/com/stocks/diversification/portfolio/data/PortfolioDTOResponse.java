package com.stocks.diversification.portfolio.data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;

@Data
@Builder
@AllArgsConstructor
public class PortfolioDTOResponse {

  private int score;
  private String assessment;
  private List<String> recommendations;

  private BigDecimal totalInvestment;
  private BigDecimal currentValue;
  private BigDecimal totalProfitLoss;
  private BigDecimal totalProfitLossPercentage;

  private Map<String, BigDecimal> sectorAllocation;

  public PortfolioDTOResponse() {
  }
}
