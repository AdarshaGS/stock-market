package com.loan.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.loan.data.Loan;

public interface LoanService {

    Loan createLoan(Loan loan);

    List<Loan> getAllLoans();

    List<Loan> getLoansByUserId(Long userId);

    Loan getLoanById(Long id);

    void deleteLoan(Long id);

    // Calculations
    BigDecimal calculateEMI(BigDecimal principal, BigDecimal rate, Integer tenureMonths);

    Map<String, Object> simulatePrepayment(Long loanId, BigDecimal prepaymentAmount);
}
