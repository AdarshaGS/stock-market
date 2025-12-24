package com.investments.stocks.diversification.portfolio.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.investments.stocks.diversification.portfolio.data.AnalysisInsight;
import com.investments.stocks.diversification.portfolio.data.PortfolioScoringResult;
import com.investments.stocks.diversification.portfolio.data.RiskAnalysisResult;
import com.investments.stocks.diversification.portfolio.data.ScoreExplanation;

@Service
public class PortfolioScoringService {

    public PortfolioScoringResult calculateScore(RiskAnalysisResult riskResult) {
        int score = 100;
        List<String> penalties = new ArrayList<>();

        // 1. Single Stock Penalty (Max exposure)
        if (riskResult.getMaxStockAllocation() > 40.0) {
            score -= 35;
            penalties.add("-35: Single stock exposure > 40%");
        } else if (riskResult.getMaxStockAllocation() > 25.0) {
            score -= 20;
            penalties.add("-20: Single stock exposure > 25%");
        }

        // 2. Single Sector Penalty
        if (riskResult.getMaxSectorAllocation() > 40.0) {
            score -= 15;
            penalties.add("-15: Sector exposure > 40%");
        }

        // 3. Small Cap Exposure
        if (riskResult.getSmallCapAllocation() > 70.0) {
            score -= 15;
            penalties.add("-15: Small-cap exposure > 70%");
        }

        // 4. Stock Drawdown
        if (riskResult.isHasSignificantDrawdown()) {
            score -= 10;
            penalties.add("-10: Stock drawdown > 25%");
        }

        // 5. Warnings Count
        long warningCount = riskResult.getInsights().stream()
                .filter(i -> i.getType() == AnalysisInsight.InsightType.WARNING)
                .count();

        if (warningCount > 3) {
            score -= 10;
            penalties.add("-10: More than 3 warnings detected");
        }

        // Clamp Score
        if (score > 90)
            score = 90;
        if (score < 30) // Clamp to 30 as per requirements
            score = 30;

        // Build Explanation
        ScoreExplanation explanation = ScoreExplanation.builder()
                .baseScore(100)
                .penalties(penalties)
                .finalScore(score)
                .build();

        // Assessment
        String assessment = getAssessment(score);

        return PortfolioScoringResult.builder()
                .score(score)
                .assessment(assessment)
                .scoreExplanation(explanation)
                .build();
    }

    private String getAssessment(int score) {
        if (score >= 80)
            return "STRONG_AND_BALANCED";
        if (score >= 65)
            return "MODERATELY_DIVERSIFIED";
        if (score >= 45)
            return "HIGH_RISK";
        return "POORLY_DIVERSIFIED";
    }
}
