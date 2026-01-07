package com.investments.mutualfunds.service;

import java.util.List;
import com.investments.mutualfunds.data.MutualFundHolding;
import com.investments.mutualfunds.data.MutualFundInsights;
import com.investments.mutualfunds.data.MutualFundSummary;

public interface MutualFundService {
    List<MutualFundHolding> getHoldings(Long userId);
    MutualFundSummary getSummary(Long userId);
    MutualFundInsights getInsights(Long userId);
}
