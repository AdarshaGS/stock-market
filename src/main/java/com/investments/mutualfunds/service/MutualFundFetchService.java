package com.investments.mutualfunds.service;

import java.util.List;
import com.investments.mutualfunds.data.MutualFundHolding;

public interface MutualFundFetchService {
    List<MutualFundHolding> fetchPortfolio(Long userId);
}
