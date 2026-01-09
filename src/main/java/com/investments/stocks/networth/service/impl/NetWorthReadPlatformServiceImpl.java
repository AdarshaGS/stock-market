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
import com.common.data.EntityType;
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
import com.investments.stocks.networth.data.AssetLiabilityTemplateDTO;
import com.investments.stocks.networth.data.EntityTemplateDTO;
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
                Map<EntityType, BigDecimal> assetBreakdown = assets.stream()
                                .collect(Collectors.groupingBy(
                                                UserAsset::getEntityType,
                                                Collectors.reducing(BigDecimal.ZERO, UserAsset::getCurrentValue,
                                                                BigDecimal::add)));

                // Add Stocks & Savings to Asset Breakdown
                assetBreakdown.merge(EntityType.STOCK, stockValue, BigDecimal::add);
                assetBreakdown.merge(EntityType.CASH, savingsValue, BigDecimal::add);

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
                assetBreakdown.merge(EntityType.LENDING, lendingOutstanding, BigDecimal::add);

                // Calculate Liability Breakdown
                Map<EntityType, BigDecimal> liabilityBreakdown = liabilities.stream()
                                .collect(Collectors.groupingBy(
                                                UserLiability::getEntityType,
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

                                        // Map LoanType to EntityType
                                        EntityType type = EntityType.OTHER;
                                        if (loan.getLoanType() != null) {
                                                switch (loan.getLoanType()) {
                                                        case HOME_LOAN:
                                                                type = EntityType.HOME_LOAN;
                                                                break;
                                                        case CAR_LOAN:
                                                                type = EntityType.CAR_LOAN;
                                                                break;
                                                        case PERSONAL_LOAN:
                                                                type = EntityType.PERSONAL_LOAN;
                                                                break;
                                                        case EDUCATION_LOAN:
                                                                type = EntityType.EDUCATION_LOAN;
                                                                break;
                                                        case CREDIT_CARD:
                                                                type = EntityType.CREDIT_CARD;
                                                                break;
                                                        case BNPL:
                                                                type = EntityType.BNPL;
                                                                break;
                                                        default:
                                                                type = EntityType.OTHER;
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

        @Override
        public AssetLiabilityTemplateDTO getEntityTemplates() {
                List<EntityTemplateDTO> assets = List.of(
                                new EntityTemplateDTO(EntityType.CASH, "Cash", "Physical cash and bank balance", "Liquid"),
                                new EntityTemplateDTO(EntityType.SAVINGS_ACCOUNT, "Savings Account", "Bank savings account", "Liquid"),
                                new EntityTemplateDTO(EntityType.FIXED_DEPOSIT, "Fixed Deposit", "Bank fixed deposit", "Investment"),
                                new EntityTemplateDTO(EntityType.RECURRING_DEPOSIT, "Recurring Deposit", "Bank recurring deposit", "Investment"),
                                new EntityTemplateDTO(EntityType.STOCK, "Stocks", "Equity shares", "Investment"),
                                new EntityTemplateDTO(EntityType.MUTUAL_FUND, "Mutual Funds", "Mutual fund units", "Investment"),
                                new EntityTemplateDTO(EntityType.ETF, "ETFs", "Exchange Traded Funds", "Investment"),
                                new EntityTemplateDTO(EntityType.GOLD, "Gold", "Physical gold or digital gold", "Asset"),
                                new EntityTemplateDTO(EntityType.REAL_ESTATE, "Real Estate", "Property and land", "Asset"),
                                new EntityTemplateDTO(EntityType.PF, "Provident Fund", "EPF, PPF, etc.", "Retirement"),
                                new EntityTemplateDTO(EntityType.LENDING, "Lendings", "Money lent to others", "Asset")
                );

                List<EntityTemplateDTO> liabilities = List.of(
                                new EntityTemplateDTO(EntityType.HOME_LOAN, "Home Loan", "Mortgage for house", "Debt"),
                                new EntityTemplateDTO(EntityType.CAR_LOAN, "Car Loan", "Loan for vehicle", "Debt"),
                                new EntityTemplateDTO(EntityType.PERSONAL_LOAN, "Personal Loan", "Unsecured personal loan", "Debt"),
                                new EntityTemplateDTO(EntityType.EDUCATION_LOAN, "Education Loan", "Loan for studies", "Debt"),
                                new EntityTemplateDTO(EntityType.CREDIT_CARD, "Credit Card", "Outstanding credit card balance", "Debt"),
                                new EntityTemplateDTO(EntityType.BNPL, "BNPL", "Buy Now Pay Later balance", "Debt"),
                                new EntityTemplateDTO(EntityType.LOAN, "Other Loan", "Any other type of loan", "Debt")
                );

                return AssetLiabilityTemplateDTO.builder()
                                .assets(assets)
                                .liabilities(liabilities)
                                .build();
        }
}
