package com.stocks.networth.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.stocks.diversification.portfolio.data.PortfolioDTOResponse;
import com.stocks.diversification.portfolio.service.PortfolioReadPlatformService;
import com.stocks.networth.data.NetWorthDTO;
import com.stocks.networth.data.UserAsset;
import com.stocks.networth.data.UserAsset.AssetType;
import com.stocks.networth.data.UserLiability;
import com.stocks.networth.data.UserLiability.LiabilityType;
import com.stocks.networth.repo.UserAssetRepository;
import com.stocks.networth.repo.UserLiabilityRepository;
import com.stocks.networth.service.NetWorthReadPlatformService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NetWorthReadPlatformServiceImpl implements NetWorthReadPlatformService {

    private final PortfolioReadPlatformService portfolioService;
    private final UserAssetRepository userAssetRepository;
    private final UserLiabilityRepository userLiabilityRepository;

    @Override
    public NetWorthDTO getNetWorth(Long userId) {
        // 1. Get Portfolio Value (Stocks)
        PortfolioDTOResponse portfolio = portfolioService.getPortfolioSummary(userId);
        BigDecimal stockValue = (portfolio != null && portfolio.getCurrentValue() != null)
                ? portfolio.getCurrentValue()
                : BigDecimal.ZERO;

        // 2. Get Other Assets
        List<UserAsset> assets = userAssetRepository.findByUserId(userId);
        
        // 3. Get Liabilities
        List<UserLiability> liabilities = userLiabilityRepository.findByUserId(userId);

        // Calculate Asset Breakdown
        Map<AssetType, BigDecimal> assetBreakdown = assets.stream()
                .collect(Collectors.groupingBy(
                        UserAsset::getAssetType,
                        Collectors.reducing(BigDecimal.ZERO, UserAsset::getCurrentValue, BigDecimal::add)
                ));
        
        // Add Stocks to Asset Breakdown
        assetBreakdown.merge(AssetType.STOCK, stockValue, BigDecimal::add);

        // Calculate Liability Breakdown
        Map<LiabilityType, BigDecimal> liabilityBreakdown = liabilities.stream()
                .collect(Collectors.groupingBy(
                        UserLiability::getLiabilityType,
                        Collectors.reducing(BigDecimal.ZERO, UserLiability::getOutstandingAmount, BigDecimal::add)
                ));

        // Calculate Totals
        BigDecimal totalAssets = assetBreakdown.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalLiabilities = liabilityBreakdown.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netWorth = totalAssets.subtract(totalLiabilities);

        return NetWorthDTO.builder()
                .totalAssets(totalAssets)
                .totalLiabilities(totalLiabilities)
                .netWorth(netWorth)
                .assetBreakdown(assetBreakdown)
                .liabilityBreakdown(liabilityBreakdown)
                .build();
    }
}
