package com.stocks.networth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.investments.stocks.diversification.portfolio.data.PortfolioDTOResponse;
import com.investments.stocks.diversification.portfolio.service.PortfolioReadPlatformService;
import com.investments.stocks.networth.data.NetWorthDTO;
import com.investments.stocks.networth.data.UserAsset;
import com.investments.stocks.networth.data.UserLiability;
import com.investments.stocks.networth.data.UserAsset.AssetType;
import com.investments.stocks.networth.data.UserLiability.LiabilityType;
import com.investments.stocks.networth.repo.UserAssetRepository;
import com.investments.stocks.networth.repo.UserLiabilityRepository;
import com.investments.stocks.networth.service.impl.NetWorthReadPlatformServiceImpl;

public class NetWorthReadPlatformServiceImplTest {

    private PortfolioReadPlatformService portfolioService;
    private UserAssetRepository userAssetRepository;
    private UserLiabilityRepository userLiabilityRepository;
    private NetWorthReadPlatformServiceImpl service;

    @BeforeEach
    void setUp() {
        portfolioService = mock(PortfolioReadPlatformService.class);
        userAssetRepository = mock(UserAssetRepository.class);
        userLiabilityRepository = mock(UserLiabilityRepository.class);
        service = new NetWorthReadPlatformServiceImpl(portfolioService, userAssetRepository, userLiabilityRepository,
                null, null, null, null, null, null);
    }

    @Test
    void testGetNetWorth_CalculatesCorrectly() {
        // Setup
        Long userId = 1L;

        // 1. Mock Portfolio (Stocks)
        PortfolioDTOResponse portfolio = PortfolioDTOResponse.builder()
                .currentValue(new BigDecimal("50000"))
                .build();
        when(portfolioService.getPortfolioSummary(userId)).thenReturn(portfolio);

        // 2. Mock Assets
        List<UserAsset> assets = List.of(
                UserAsset.builder().assetType(AssetType.SAVINGS).currentValue(new BigDecimal("10000")).build(),
                UserAsset.builder().assetType(AssetType.PF).currentValue(new BigDecimal("20000")).build());
        when(userAssetRepository.findByUserId(userId)).thenReturn(assets);

        // 3. Mock Liabilities
        List<UserLiability> liabilities = List.of(
                UserLiability.builder().liabilityType(LiabilityType.CREDIT_CARD)
                        .outstandingAmount(new BigDecimal("5000")).build());
        when(userLiabilityRepository.findByUserId(userId)).thenReturn(liabilities);

        // Execute
        NetWorthDTO result = service.getNetWorth(userId);

        // Verify
        // Total Assets = 50000 (Stocks) + 10000 (Savings) + 20000 (PF) = 80000
        assertEquals(new BigDecimal("80000"), result.getTotalAssets());

        // Total Liabilities = 5000
        assertEquals(new BigDecimal("5000"), result.getTotalLiabilities());

        // Net Worth = 80000 - 5000 = 75000
        assertEquals(new BigDecimal("75000"), result.getNetWorth());

        // Breakdowns
        assertEquals(new BigDecimal("50000"), result.getAssetBreakdown().get(AssetType.STOCK));
        assertEquals(new BigDecimal("10000"), result.getAssetBreakdown().get(AssetType.SAVINGS));
    }

    @Test
    void testGetNetWorth_HandlesEmptyPortfolio() {
        // Setup
        Long userId = 2L;

        when(portfolioService.getPortfolioSummary(userId)).thenReturn(null);
        when(userAssetRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(userLiabilityRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        // Execute
        NetWorthDTO result = service.getNetWorth(userId);

        // Verify
        assertEquals(BigDecimal.ZERO, result.getTotalAssets());
        assertEquals(BigDecimal.ZERO, result.getNetWorth());
    }
}
