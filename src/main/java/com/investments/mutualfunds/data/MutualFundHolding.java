package com.investments.mutualfunds.data;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MutualFundHolding {
    private String amc;
    private String schemeName;
    private String folioNumber;
    private BigDecimal units;
    private BigDecimal nav;
    private BigDecimal currentValue;
    private BigDecimal costValue;
    private MFAssetType assetType;
    private List<MFTransaction> transactions;
}
