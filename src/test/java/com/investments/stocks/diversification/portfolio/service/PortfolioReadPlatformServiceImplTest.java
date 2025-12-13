package com.investments.stocks.diversification.portfolio.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.investments.stocks.data.Stock;
import com.investments.stocks.diversification.portfolio.data.Portfolio;
import com.investments.stocks.diversification.portfolio.data.PortfolioDTOResponse;
import com.investments.stocks.diversification.portfolio.repo.PortfolioRepository;
import com.investments.stocks.diversification.sectors.data.Sector;
import com.investments.stocks.diversification.sectors.repo.SectorRepository;
import com.investments.stocks.repo.StockRepository;
import com.loan.data.Loan;
import com.loan.service.LoanService;
import com.protection.insurance.data.Insurance;
import com.protection.insurance.service.InsuranceService;
import com.savings.data.FixedDepositDTO;
import com.savings.data.RecurringDepositDTO;
import com.savings.data.SavingsAccountDTO;
import com.savings.service.FixedDepositService;
import com.savings.service.RecurringDepositService;
import com.savings.service.SavingsAccountService;

@ExtendWith(MockitoExtension.class)
class PortfolioReadPlatformServiceImplTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private SectorRepository sectorRepository;

    @Mock
    private PortfolioAnalyzerEngine portfolioAnalyzerEngine;

    @Mock
    private SavingsAccountService savingsAccountService;

    @Mock
    private FixedDepositService fixedDepositService;

    @Mock
    private RecurringDepositService recurringDepositService;

    @Mock
    private LoanService loanService;

    @Mock
    private InsuranceService insuranceService;

    @InjectMocks
    private PortfolioReadPlatformServiceImpl portfolioService;

    private Long userId;

    @BeforeEach
    void setUp() {
        userId = 1L;
    }

    @Test
    void testGetPortfolioSummary_WithExtendedData() {
        // Arrange
        Portfolio portfolio = new Portfolio();
        portfolio.setUserId(userId);
        portfolio.setStockSymbol("AAPL");
        portfolio.setQuantity(10);
        portfolio.setPurchasePrice(new BigDecimal("150"));

        Stock stock = new Stock();
        stock.setSymbol("AAPL");
        stock.setPrice(200.0);
        stock.setMarketCap(25000.0);
        stock.setSectorId(1L);

        Sector sector = new Sector();
        sector.setId(1L);
        sector.setName("Technology");

        List<SavingsAccountDTO> savings = Arrays.asList(
                SavingsAccountDTO.builder().amount(new BigDecimal("100000")).build()
        );

        List<FixedDepositDTO> fds = Arrays.asList(
                FixedDepositDTO.builder()
                        .principalAmount(new BigDecimal("50000"))
                        .maturityAmount(new BigDecimal("55000"))
                        .build()
        );

        List<RecurringDepositDTO> rds = Arrays.asList(
                RecurringDepositDTO.builder()
                        .maturityAmount(new BigDecimal("30000"))
                        .build()
        );

        Loan loan = new Loan();
        loan.setOutstandingAmount(new BigDecimal("200000"));

        Insurance insurance = new Insurance();
        insurance.setCoverAmount(new BigDecimal("5000000"));

        when(portfolioRepository.findByUserId(userId)).thenReturn(Arrays.asList(portfolio));
        when(stockRepository.findBySymbolIn(anyList())).thenReturn(Arrays.asList(stock));
        when(sectorRepository.findAllById(anySet())).thenReturn(Arrays.asList(sector));
        when(portfolioAnalyzerEngine.analyze(anyList(), anyMap(), anyMap())).thenReturn(Arrays.asList());
        when(portfolioAnalyzerEngine.calculateHealthScore(anyList())).thenReturn(85);
        when(savingsAccountService.getAllSavingsAccounts(userId)).thenReturn(savings);
        when(fixedDepositService.getAllFixedDeposits(userId)).thenReturn(fds);
        when(recurringDepositService.getAllRecurringDeposits(userId)).thenReturn(rds);
        when(loanService.getLoansByUserId(userId)).thenReturn(Arrays.asList(loan));
        when(insuranceService.getInsurancePoliciesByUserId(userId)).thenReturn(Arrays.asList(insurance));

        // Act
        PortfolioDTOResponse result = portfolioService.getPortfolioSummary(userId);

        // Assert
        assertNotNull(result);
        
        // Portfolio calculations
        assertEquals(0, result.getTotalInvestment().compareTo(new BigDecimal("1500"))); // 10 * 150
        assertEquals(0, result.getCurrentValue().compareTo(new BigDecimal("2000"))); // 10 * 200
        assertEquals(0, result.getTotalProfitLoss().compareTo(new BigDecimal("500"))); // 2000 - 1500

        // Extended data
        assertEquals(0, result.getSavingsTotal().compareTo(new BigDecimal("185000"))); // 100k + 55k + 30k
        assertEquals(0, result.getLoansOutstanding().compareTo(new BigDecimal("200000")));
        assertEquals(0, result.getInsuranceCoverTotal().compareTo(new BigDecimal("5000000")));

        verify(savingsAccountService).getAllSavingsAccounts(userId);
        verify(fixedDepositService).getAllFixedDeposits(userId);
        verify(recurringDepositService).getAllRecurringDeposits(userId);
        verify(loanService).getLoansByUserId(userId);
        verify(insuranceService).getInsurancePoliciesByUserId(userId);
    }

    @Test
    void testGetPortfolioSummary_EmptyPortfolio() {
        // Arrange
        when(portfolioRepository.findByUserId(userId)).thenReturn(Arrays.asList());

        // Act
        PortfolioDTOResponse result = portfolioService.getPortfolioSummary(userId);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getTotalInvestment());
        assertEquals(BigDecimal.ZERO, result.getCurrentValue());
        assertEquals(BigDecimal.ZERO, result.getTotalProfitLoss());
        assertEquals(0, result.getScore());
        assertEquals("No Data", result.getAssessment());
    }

    @Test
    void testGetPortfolioSummary_WithServiceExceptions() {
        // Arrange
        Portfolio portfolio = new Portfolio();
        portfolio.setUserId(userId);
        portfolio.setStockSymbol("AAPL");
        portfolio.setQuantity(10);
        portfolio.setPurchasePrice(new BigDecimal("150"));

        when(portfolioRepository.findByUserId(userId)).thenReturn(Arrays.asList(portfolio));
        when(stockRepository.findBySymbolIn(anyList())).thenReturn(Arrays.asList());
        when(sectorRepository.findAllById(anySet())).thenReturn(Arrays.asList());
        when(portfolioAnalyzerEngine.analyze(anyList(), anyMap(), anyMap())).thenReturn(Arrays.asList());
        when(portfolioAnalyzerEngine.calculateHealthScore(anyList())).thenReturn(0);
        
        // Services throw exceptions
        when(savingsAccountService.getAllSavingsAccounts(userId)).thenThrow(new RuntimeException("Service error"));
        when(fixedDepositService.getAllFixedDeposits(userId)).thenThrow(new RuntimeException("Service error"));
        when(recurringDepositService.getAllRecurringDeposits(userId)).thenThrow(new RuntimeException("Service error"));
        when(loanService.getLoansByUserId(userId)).thenThrow(new RuntimeException("Service error"));
        when(insuranceService.getInsurancePoliciesByUserId(userId)).thenThrow(new RuntimeException("Service error"));

        // Act
        PortfolioDTOResponse result = portfolioService.getPortfolioSummary(userId);

        // Assert - Should not throw, should return zero for extended fields
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getSavingsTotal());
        assertEquals(BigDecimal.ZERO, result.getLoansOutstanding());
        assertEquals(BigDecimal.ZERO, result.getInsuranceCoverTotal());
    }

    @Test
    void testGetPortfolioSummary_WithNullAmounts() {
        // Arrange
        Portfolio portfolio = new Portfolio();
        portfolio.setUserId(userId);
        portfolio.setStockSymbol("AAPL");
        portfolio.setQuantity(10);
        portfolio.setPurchasePrice(new BigDecimal("150"));

        Loan loanWithNull = new Loan();
        loanWithNull.setOutstandingAmount(null);

        Insurance insuranceWithNull = new Insurance();
        insuranceWithNull.setCoverAmount(null);

        when(portfolioRepository.findByUserId(userId)).thenReturn(Arrays.asList(portfolio));
        when(stockRepository.findBySymbolIn(anyList())).thenReturn(Arrays.asList());
        when(sectorRepository.findAllById(anySet())).thenReturn(Arrays.asList());
        when(portfolioAnalyzerEngine.analyze(anyList(), anyMap(), anyMap())).thenReturn(Arrays.asList());
        when(portfolioAnalyzerEngine.calculateHealthScore(anyList())).thenReturn(0);
        when(savingsAccountService.getAllSavingsAccounts(userId)).thenReturn(Arrays.asList());
        when(fixedDepositService.getAllFixedDeposits(userId)).thenReturn(Arrays.asList());
        when(recurringDepositService.getAllRecurringDeposits(userId)).thenReturn(Arrays.asList());
        when(loanService.getLoansByUserId(userId)).thenReturn(Arrays.asList(loanWithNull));
        when(insuranceService.getInsurancePoliciesByUserId(userId)).thenReturn(Arrays.asList(insuranceWithNull));

        // Act
        PortfolioDTOResponse result = portfolioService.getPortfolioSummary(userId);

        // Assert - Should handle nulls gracefully
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getLoansOutstanding());
        assertEquals(BigDecimal.ZERO, result.getInsuranceCoverTotal());
    }
}
