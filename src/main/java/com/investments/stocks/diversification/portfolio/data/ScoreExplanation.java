package com.investments.stocks.diversification.portfolio.data;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScoreExplanation {
    private int baseScore;
    private List<String> penalties;
    private int finalScore;
}
