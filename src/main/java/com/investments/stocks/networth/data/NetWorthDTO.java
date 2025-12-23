package com.investments.stocks.networth.data;

import lombok.*;
import java.math.BigDecimal;
import java.util.Map;

import com.investments.stocks.networth.data.UserAsset.AssetType;
import com.investments.stocks.networth.data.UserLiability.LiabilityType;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class NetWorthDTO {
    private BigDecimal totalAssets;
    private BigDecimal totalLiabilities;
    private BigDecimal netWorth;
    private BigDecimal portfolioValue;
    private BigDecimal savingsValue;
    private BigDecimal outstandingLoans;
    private BigDecimal outstandingTaxLiability;
    private BigDecimal outstandingLendings;
    private BigDecimal netWorthAfterTax;
    private Map<AssetType, BigDecimal> assetBreakdown;
    private Map<LiabilityType, BigDecimal> liabilityBreakdown;
}
