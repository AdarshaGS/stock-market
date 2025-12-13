package com.investments.stocks.networth.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.investments.stocks.diversification.portfolio.data.PortfolioDTOResponse;
import com.investments.stocks.diversification.portfolio.service.PortfolioReadPlatformService;
import com.investments.stocks.networth.data.NetWorthDTO;
import com.investments.stocks.networth.data.UserAsset;
import com.investments.stocks.networth.data.UserLiability;
import com.investments.stocks.networth.data.UserAsset.AssetType;
import com.investments.stocks.networth.data.UserLiability.LiabilityType;
import com.investments.stocks.networth.repo.UserAssetRepository;
import com.investments.stocks.networth.repo.UserLiabilityRepository;
import com.investments.stocks.networth.service.NetWorthReadPlatformService;
import com.loan.service.LoanService;
import com.savings.service.SavingsAccountService;
import com.savings.service.FixedDepositService;
import com.savings.service.RecurringDepositService;
import com.savings.data.SavingsAccountDTO;
import com.savings.data.FixedDepositDTO;
import com.savings.data.RecurringDepositDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NetWorthReadPlatformServiceImpl implements NetWorthReadPlatformService {

        private final PortfolioReadPlatformService portfolioService;
        private final UserAssetRepository userAssetRepository;
        private final UserLiabilityRepository userLiabilityRepository;
        private final SavingsAccountService savingsAccountService;
        private final FixedDepositService fixedDepositService;
        private final RecurringDepositService recurringDepositService;
        private final LoanService loanService;

    @Override
    public NetWorthDTO getNetWorth(Long userId) {
        // 1. Get Portfolio Value (Stocks)
        PortfolioDTOResponse portfolio = portfolioService.getPortfolioSummary(userId);
        BigDecimal stockValue = (portfolio != null && portfolio.getCurrentValue() != null)
                ? portfolio.getCurrentValue()
                : BigDecimal.ZERO;

                // 1.b Get Savings (cash + FD + RD) balance
                BigDecimal savingsValue = BigDecimal.ZERO;
                try {
                        List<SavingsAccountDTO> savings = savingsAccountService.getAllSavingsAccounts(userId);
                        if (savings != null) {
                                savingsValue = savings.stream()
                                        .map(SavingsAccountDTO::getAmount)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                        }
                } catch (Exception ignored) {
                        // keep savingsValue as ZERO if not available
                }
                
                // Add Fixed Deposits to savings
                try {
                        List<FixedDepositDTO> fds = fixedDepositService.getAllFixedDeposits(userId);
                        if (fds != null) {
                                BigDecimal fdValue = fds.stream()
                                        .map(fd -> fd.getMaturityAmount() != null ? fd.getMaturityAmount() : fd.getPrincipalAmount())
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                savingsValue = savingsValue.add(fdValue);
                        }
                } catch (Exception ignored) {}
                
                // Add Recurring Deposits to savings
                try {
                        List<RecurringDepositDTO> rds = recurringDepositService.getAllRecurringDeposits(userId);
                        if (rds != null) {
                                BigDecimal rdValue = rds.stream()
                                        .map(rd -> rd.getMaturityAmount() != null ? rd.getMaturityAmount() : BigDecimal.ZERO)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                savingsValue = savingsValue.add(rdValue);
                        }
                } catch (Exception ignored) {}

        // 2. Get Other Assets
        List<UserAsset> assets = userAssetRepository.findByUserId(userId);
        
        // 3. Get Liabilities
                List<UserLiability> liabilities = userLiabilityRepository.findByUserId(userId);

                // 3.b Include Loans outstanding as liabilities
                BigDecimal loansOutstanding = BigDecimal.ZERO;
                try {
                        loansOutstanding = loanService.getLoansByUserId(userId).stream()
                                        .map(l -> l.getOutstandingAmount() != null ? l.getOutstandingAmount() : BigDecimal.ZERO)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                } catch (Exception ignored) {
                        // keep loansOutstanding as ZERO if not available
                }

        // Calculate Asset Breakdown
        Map<AssetType, BigDecimal> assetBreakdown = assets.stream()
                .collect(Collectors.groupingBy(
                        UserAsset::getAssetType,
                        Collectors.reducing(BigDecimal.ZERO, UserAsset::getCurrentValue, BigDecimal::add)
                ));
        
        // Add Stocks & Savings to Asset Breakdown
        assetBreakdown.merge(AssetType.STOCK, stockValue, BigDecimal::add);
        assetBreakdown.merge(AssetType.CASH, savingsValue, BigDecimal::add);

        // Calculate Liability Breakdown
        Map<LiabilityType, BigDecimal> liabilityBreakdown = liabilities.stream()
                .collect(Collectors.groupingBy(
                        UserLiability::getLiabilityType,
                        Collectors.reducing(BigDecimal.ZERO, UserLiability::getOutstandingAmount, BigDecimal::add)
                ));

        // Add Loans to Liability Breakdown (aggregate into OTHER if specific type unknown)
        liabilityBreakdown.merge(LiabilityType.OTHER, loansOutstanding, BigDecimal::add);

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
