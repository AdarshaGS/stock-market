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
import com.loan.data.Loan;
import com.loan.service.LoanService;
import com.savings.service.SavingsAccountService;
import com.tax.service.TaxService;
import com.savings.service.FixedDepositService;
import com.savings.service.RecurringDepositService;
import com.savings.data.SavingsAccountDTO;
import com.savings.data.FixedDepositDTO;
import com.savings.data.RecurringDepositDTO;
import com.lending.service.LendingService;
import com.lending.data.LendingDTO;

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
        private final TaxService taxService;
        private final LendingService lendingService;

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
                                                .map(fd -> fd.getMaturityAmount() != null ? fd.getMaturityAmount()
                                                                : fd.getPrincipalAmount())
                                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                                savingsValue = savingsValue.add(fdValue);
                        }
                } catch (Exception ignored) {
                }

                // Add Recurring Deposits to savings
                try {
                        List<RecurringDepositDTO> rds = recurringDepositService.getAllRecurringDeposits(userId);
                        if (rds != null) {
                                BigDecimal rdValue = rds.stream()
                                                .map(rd -> rd.getMaturityAmount() != null ? rd.getMaturityAmount()
                                                                : BigDecimal.ZERO)
                                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                                savingsValue = savingsValue.add(rdValue);
                        }
                } catch (Exception ignored) {
                }

                // 2. Get Other Assets
                List<UserAsset> assets = userAssetRepository.findByUserId(userId);

                // 3. Get Liabilities
                List<UserLiability> liabilities = userLiabilityRepository.findByUserId(userId);

                // Calculate Asset Breakdown
                Map<AssetType, BigDecimal> assetBreakdown = assets.stream()
                                .collect(Collectors.groupingBy(
                                                UserAsset::getAssetType,
                                                Collectors.reducing(BigDecimal.ZERO, UserAsset::getCurrentValue,
                                                                BigDecimal::add)));

                // Add Stocks & Savings to Asset Breakdown
                assetBreakdown.merge(AssetType.STOCK, stockValue, BigDecimal::add);
                assetBreakdown.merge(AssetType.CASH, savingsValue, BigDecimal::add);

                // Add Lending outstanding as asset
                BigDecimal lendingOutstanding = BigDecimal.ZERO;
                try {
                        List<LendingDTO> lendings = lendingService.getUserLendings(userId);
                        if (lendings != null) {
                                lendingOutstanding = lendings.stream()
                                                .map(LendingDTO::getOutstandingAmount)
                                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        }
                } catch (Exception ignored) {
                }
                assetBreakdown.merge(AssetType.LENDING, lendingOutstanding, BigDecimal::add);

                // Calculate Liability Breakdown
                Map<LiabilityType, BigDecimal> liabilityBreakdown = liabilities.stream()
                                .collect(Collectors.groupingBy(
                                                UserLiability::getLiabilityType,
                                                Collectors.reducing(BigDecimal.ZERO,
                                                                UserLiability::getOutstandingAmount, BigDecimal::add)));

                // 3.b Include Loans outstanding as liabilities
                BigDecimal loansOutstanding = BigDecimal.ZERO;
                try {
                        List<Loan> loans = loanService.getLoansByUserId(userId);
                        if (loans != null) {
                                for (Loan loan : loans) {
                                        BigDecimal amount = loan.getOutstandingAmount() != null
                                                        ? loan.getOutstandingAmount()
                                                        : BigDecimal.ZERO;
                                        loansOutstanding = loansOutstanding.add(amount);

                                        // Map LoanType to LiabilityType
                                        LiabilityType type = LiabilityType.OTHER;
                                        if (loan.getLoanType() != null) {
                                                switch (loan.getLoanType()) {
                                                        case HOME_LOAN:
                                                                type = LiabilityType.HOME_LOAN;
                                                                break;
                                                        case CAR_LOAN:
                                                                type = LiabilityType.CAR_LOAN;
                                                                break;
                                                        case PERSONAL_LOAN:
                                                                type = LiabilityType.PERSONAL_LOAN;
                                                                break;
                                                        case EDUCATION_LOAN:
                                                                type = LiabilityType.EDUCATION_LOAN;
                                                                break;
                                                        case CREDIT_CARD:
                                                                type = LiabilityType.CREDIT_CARD;
                                                                break;
                                                        case BNPL:
                                                                type = LiabilityType.BNPL;
                                                                break;
                                                        default:
                                                                type = LiabilityType.OTHER;
                                                                break;
                                                }
                                        }
                                        liabilityBreakdown.merge(type, amount, BigDecimal::add);
                                }
                        }
                } catch (Exception ignored) {
                        // keep loansOutstanding as ZERO if not available
                }

                // Calculate Totals
                BigDecimal totalAssets = assetBreakdown.values().stream()
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Liabilities excluding Tax (Pre-Tax Debt)
                BigDecimal totalLiabilitiesPreTax = liabilityBreakdown.values().stream()
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal taxLiability = taxService.getOutstandingTaxLiability(userId);

                // Total Liabilities including Tax
                BigDecimal totalLiabilitiesPostTax = totalLiabilitiesPreTax.add(taxLiability);

                BigDecimal netWorthPreTax = totalAssets.subtract(totalLiabilitiesPreTax);
                BigDecimal netWorthPostTax = totalAssets.subtract(totalLiabilitiesPostTax);

                return NetWorthDTO.builder()
                                .totalAssets(totalAssets)
                                .totalLiabilities(totalLiabilitiesPreTax)
                                .netWorth(netWorthPreTax)
                                .portfolioValue(stockValue)
                                .savingsValue(savingsValue)
                                .outstandingLoans(loansOutstanding)
                                .outstandingTaxLiability(taxLiability)
                                .outstandingLendings(lendingOutstanding)
                                .netWorthAfterTax(netWorthPostTax)
                                .assetBreakdown(assetBreakdown)
                                .liabilityBreakdown(liabilityBreakdown)
                                .build();
        }
}
