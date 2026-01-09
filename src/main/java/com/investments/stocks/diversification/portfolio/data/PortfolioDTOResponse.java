package com.investments.stocks.diversification.portfolio.data;

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

  // Smart Analysis Fields
  // private int healthScore; // Removed internally
  private PortfolioInsightsDTO insights; // Grouped insights
  private RiskSummary riskSummary;
  private NextBestAction nextBestAction;
  private ScoreExplanation scoreExplanation;
  private MarketCapAllocation marketCapAllocation;
  private DataFreshness dataFreshness;

  // Extended summary fields
  // Removed savings, loans, and insurance as per requirements

  public PortfolioDTOResponse() {
  }
}
