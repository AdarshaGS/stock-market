package com.investments.stocks.networth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.investments.stocks.diversification.portfolio.data.PortfolioDTOResponse;
import com.investments.stocks.diversification.portfolio.service.PortfolioReadPlatformService;
import com.investments.stocks.networth.data.NetWorthDTO;
import com.investments.stocks.networth.data.UserAsset;
import com.investments.stocks.networth.data.UserAsset.AssetType;
import com.investments.stocks.networth.data.UserLiability;
import com.investments.stocks.networth.data.UserLiability.LiabilityType;
import com.investments.stocks.networth.repo.UserAssetRepository;
import com.investments.stocks.networth.repo.UserLiabilityRepository;
import com.investments.stocks.networth.service.impl.NetWorthReadPlatformServiceImpl;
import com.loan.data.Loan;
import com.loan.service.LoanService;
import com.savings.data.FixedDepositDTO;
import com.savings.data.RecurringDepositDTO;
import com.savings.data.SavingsAccountDTO;
import com.savings.service.FixedDepositService;
import com.savings.service.RecurringDepositService;
import com.savings.service.SavingsAccountService;

@ExtendWith(MockitoExtension.class)
class NetWorthReadPlatformServiceImplTest {

    @Mock
    private PortfolioReadPlatformService portfolioService;

    @Mock
    private UserAssetRepository userAssetRepository;

    @Mock
    private UserLiabilityRepository userLiabilityRepository;

    @Mock
    private SavingsAccountService savingsAccountService;

    @Mock
    private FixedDepositService fixedDepositService;

    @Mock
    private RecurringDepositService recurringDepositService;

    @Mock
    private LoanService loanService;

    @InjectMocks
    private NetWorthReadPlatformServiceImpl netWorthService;

    private Long userId;

    @BeforeEach
    void setUp() {
        userId = 1L;
    }

    @Test
    void testGetNetWorth_WithAllComponents() {
        // Arrange
        PortfolioDTOResponse portfolio = PortfolioDTOResponse.builder()
                .currentValue(new BigDecimal("500000"))
                .build();

        List<SavingsAccountDTO> savings = Arrays.asList(
                SavingsAccountDTO.builder().amount(new BigDecimal("50000")).build(),
                SavingsAccountDTO.builder().amount(new BigDecimal("30000")).build()
        );

        List<FixedDepositDTO> fds = Arrays.asList(
                FixedDepositDTO.builder()
                        .principalAmount(new BigDecimal("100000"))
                        .maturityAmount(new BigDecimal("110000"))
                        .build()
        );

        List<RecurringDepositDTO> rds = Arrays.asList(
                RecurringDepositDTO.builder()
                        .maturityAmount(new BigDecimal("60000"))
                        .build()
        );

        List<UserAsset> assets = Arrays.asList(
                UserAsset.builder()
                        .assetType(AssetType.GOLD)
                        .currentValue(new BigDecimal("200000"))
                        .build()
        );

        Loan loan = new Loan();
        loan.setOutstandingAmount(new BigDecimal("300000"));
        List<Loan> loans = Arrays.asList(loan);

        List<UserLiability> liabilities = Arrays.asList(
                UserLiability.builder()
                        .liabilityType(LiabilityType.CREDIT_CARD)
                        .outstandingAmount(new BigDecimal("50000"))
                        .build()
        );

        when(portfolioService.getPortfolioSummary(userId)).thenReturn(portfolio);
        when(savingsAccountService.getAllSavingsAccounts(userId)).thenReturn(savings);
        when(fixedDepositService.getAllFixedDeposits(userId)).thenReturn(fds);
        when(recurringDepositService.getAllRecurringDeposits(userId)).thenReturn(rds);
        when(userAssetRepository.findByUserId(userId)).thenReturn(assets);
        when(loanService.getLoansByUserId(userId)).thenReturn(loans);
        when(userLiabilityRepository.findByUserId(userId)).thenReturn(liabilities);

        // Act
        NetWorthDTO result = netWorthService.getNetWorth(userId);

        // Assert
        assertNotNull(result);
        
        // Total Assets = Stocks(500k) + Savings(80k) + FD(110k) + RD(60k) + Gold(200k) = 950k
        assertEquals(new BigDecimal("950000"), result.getTotalAssets());
        
        // Total Liabilities = Loans(300k) + Credit Card(50k) = 350k
        assertEquals(new BigDecimal("350000"), result.getTotalLiabilities());
        
        // Net Worth = 950k - 350k = 600k
        assertEquals(new BigDecimal("600000"), result.getNetWorth());

        // Verify asset breakdown
        Map<AssetType, BigDecimal> assetBreakdown = result.getAssetBreakdown();
        assertEquals(new BigDecimal("500000"), assetBreakdown.get(AssetType.STOCK));
        assertEquals(new BigDecimal("250000"), assetBreakdown.get(AssetType.CASH)); // 80k+110k+60k
        assertEquals(new BigDecimal("200000"), assetBreakdown.get(AssetType.GOLD));

        // Verify liability breakdown
        Map<LiabilityType, BigDecimal> liabilityBreakdown = result.getLiabilityBreakdown();
        assertEquals(new BigDecimal("300000"), liabilityBreakdown.get(LiabilityType.OTHER)); // Loans
        assertEquals(new BigDecimal("50000"), liabilityBreakdown.get(LiabilityType.CREDIT_CARD));
    }

    @Test
    void testGetNetWorth_WithNoPortfolio() {
        // Arrange
        when(portfolioService.getPortfolioSummary(userId)).thenReturn(null);
        when(savingsAccountService.getAllSavingsAccounts(userId)).thenReturn(Arrays.asList());
        when(fixedDepositService.getAllFixedDeposits(userId)).thenReturn(Arrays.asList());
        when(recurringDepositService.getAllRecurringDeposits(userId)).thenReturn(Arrays.asList());
        when(userAssetRepository.findByUserId(userId)).thenReturn(Arrays.asList());
        when(loanService.getLoansByUserId(userId)).thenReturn(Arrays.asList());
        when(userLiabilityRepository.findByUserId(userId)).thenReturn(Arrays.asList());

        // Act
        NetWorthDTO result = netWorthService.getNetWorth(userId);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getTotalAssets());
        assertEquals(BigDecimal.ZERO, result.getTotalLiabilities());
        assertEquals(BigDecimal.ZERO, result.getNetWorth());
    }

    @Test
    void testGetNetWorth_WithServiceExceptions() {
        // Arrange
        when(portfolioService.getPortfolioSummary(userId)).thenThrow(new RuntimeException("Service error"));
        when(savingsAccountService.getAllSavingsAccounts(userId)).thenThrow(new RuntimeException("Service error"));
        when(fixedDepositService.getAllFixedDeposits(userId)).thenThrow(new RuntimeException("Service error"));
        when(recurringDepositService.getAllRecurringDeposits(userId)).thenThrow(new RuntimeException("Service error"));
        when(userAssetRepository.findByUserId(userId)).thenReturn(Arrays.asList());
        when(loanService.getLoansByUserId(userId)).thenThrow(new RuntimeException("Service error"));
        when(userLiabilityRepository.findByUserId(userId)).thenReturn(Arrays.asList());

        // Act
        NetWorthDTO result = netWorthService.getNetWorth(userId);

        // Assert - Should not throw, should return zero values
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getTotalAssets());
        assertEquals(BigDecimal.ZERO, result.getTotalLiabilities());
        assertEquals(BigDecimal.ZERO, result.getNetWorth());
    }

    @Test
    void testGetNetWorth_WithNullOutstandingAmounts() {
        // Arrange
        Loan loanWithNullAmount = new Loan();
        loanWithNullAmount.setOutstandingAmount(null);
        List<Loan> loans = Arrays.asList(loanWithNullAmount);

        when(portfolioService.getPortfolioSummary(userId)).thenReturn(
                PortfolioDTOResponse.builder().currentValue(BigDecimal.ZERO).build());
        when(savingsAccountService.getAllSavingsAccounts(userId)).thenReturn(Arrays.asList());
        when(fixedDepositService.getAllFixedDeposits(userId)).thenReturn(Arrays.asList());
        when(recurringDepositService.getAllRecurringDeposits(userId)).thenReturn(Arrays.asList());
        when(userAssetRepository.findByUserId(userId)).thenReturn(Arrays.asList());
        when(loanService.getLoansByUserId(userId)).thenReturn(loans);
        when(userLiabilityRepository.findByUserId(userId)).thenReturn(Arrays.asList());

        // Act
        NetWorthDTO result = netWorthService.getNetWorth(userId);

        // Assert - Should handle null gracefully
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getTotalLiabilities());
    }

    @Test
    void testGetNetWorth_WithFDHavingNullMaturityAmount() {
        // Arrange
        List<FixedDepositDTO> fds = Arrays.asList(
                FixedDepositDTO.builder()
                        .principalAmount(new BigDecimal("100000"))
                        .maturityAmount(null)
                        .build()
        );

        when(portfolioService.getPortfolioSummary(userId)).thenReturn(
                PortfolioDTOResponse.builder().currentValue(BigDecimal.ZERO).build());
        when(savingsAccountService.getAllSavingsAccounts(userId)).thenReturn(Arrays.asList());
        when(fixedDepositService.getAllFixedDeposits(userId)).thenReturn(fds);
        when(recurringDepositService.getAllRecurringDeposits(userId)).thenReturn(Arrays.asList());
        when(userAssetRepository.findByUserId(userId)).thenReturn(Arrays.asList());
        when(loanService.getLoansByUserId(userId)).thenReturn(Arrays.asList());
        when(userLiabilityRepository.findByUserId(userId)).thenReturn(Arrays.asList());

        // Act
        NetWorthDTO result = netWorthService.getNetWorth(userId);

        // Assert - Should use principalAmount when maturityAmount is null
        assertNotNull(result);
        assertEquals(new BigDecimal("100000"), result.getTotalAssets());
    }
}
