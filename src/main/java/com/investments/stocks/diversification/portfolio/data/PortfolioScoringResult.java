package com.investments.stocks.diversification.portfolio.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PortfolioScoringResult {
    private int score;
    private String assessment;
    private ScoreExplanation scoreExplanation;
}
