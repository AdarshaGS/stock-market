package com.investments.stocks.diversification.portfolio.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NextBestAction {
    private String title;
    private String description;
    private String urgency; // "HIGH", "MEDIUM", "LOW"
}
