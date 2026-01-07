package com.investments.mutualfunds.data;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MutualFundSummary {
    private BigDecimal totalCurrentValue;
    private BigDecimal totalCostValue;
    private BigDecimal totalUnrealizedGain;
    private double xirr;
}
