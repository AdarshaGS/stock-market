package com.investments.mutualfunds.data;

import java.math.BigDecimal;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MutualFundInsights {
    private Map<String, BigDecimal> assetAllocation; // EQUITY -> 1000.00
    private Map<String, BigDecimal> amcConcentration; // HDFC -> 5000.00
    private Map<String, BigDecimal> riskBuckets; // HIGH -> 2000.00
    private double portfolioXirr;
}
