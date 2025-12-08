package com.stocks.networth.data;

import java.math.BigDecimal;
import java.util.Map;

import com.stocks.networth.data.UserAsset.AssetType;
import com.stocks.networth.data.UserLiability.LiabilityType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NetWorthDTO {
    private BigDecimal totalAssets;
    private BigDecimal totalLiabilities;
    private BigDecimal netWorth;
    private Map<AssetType, BigDecimal> assetBreakdown;
    private Map<LiabilityType, BigDecimal> liabilityBreakdown;
}
