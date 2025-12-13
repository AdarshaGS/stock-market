package com.loan.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.loan.data.Loan;
import com.loan.repo.LoanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);

    @Override
    public Loan createLoan(Loan loan) {
        if (loan.getEmiAmount() == null && loan.getPrincipalAmount() != null
                && loan.getInterestRate() != null && loan.getTenureMonths() != null) {
            loan.setEmiAmount(calculateEMI(loan.getPrincipalAmount(), loan.getInterestRate(), loan.getTenureMonths()));
        }
        if (loan.getOutstandingAmount() == null) {
            loan.setOutstandingAmount(loan.getPrincipalAmount());
        }
        calculateEndDate(loan);
        return loanRepository.save(loan);
    }

    private void calculateEndDate(Loan loan) {
        if (loan.getStartDate() != null && loan.getTenureMonths() != null) {
            loan.setEndDate(loan.getStartDate().plusMonths(loan.getTenureMonths()));
        }
    }

    @Override
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    @Override
    public List<Loan> getLoansByUserId(Long userId) {
        return loanRepository.findByUserId(userId);
    }

    @Override
    public Loan getLoanById(Long id) {
        return loanRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteLoan(Long id) {
        loanRepository.deleteById(id);
    }

    @Override
    public BigDecimal calculateEMI(BigDecimal principal, BigDecimal rate, Integer tenureMonths) {
        if (principal == null || rate == null || tenureMonths == null || tenureMonths == 0) {
            return BigDecimal.ZERO;
        }

        // r = rate / 12 / 100
        BigDecimal monthlyRate = rate.divide(BigDecimal.valueOf(1200), MC);

        // (1+r)^n
        BigDecimal onePlusRToN = monthlyRate.add(BigDecimal.ONE).pow(tenureMonths, MC);

        // Numerator: P * r * (1+r)^n
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRToN);

        // Denominator: (1+r)^n - 1
        BigDecimal denominator = onePlusRToN.subtract(BigDecimal.ONE);

        if (denominator.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    @Override
    public Map<String, Object> simulatePrepayment(Long loanId, BigDecimal prepaymentAmount) {
        Loan loan = getLoanById(loanId);
        if (loan == null) {
            throw new RuntimeException("Loan not found"); // Should handle better
        }

        BigDecimal currentOutstanding = loan.getOutstandingAmount();
        BigDecimal newPrincipal = currentOutstanding.subtract(prepaymentAmount);

        if (newPrincipal.compareTo(BigDecimal.ZERO) <= 0) {
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Loan fully paid off!");
            result.put("newTenureMonths", 0);
            return result;
        }

        // Calculate new tenure keeping EMI same
        // n = -log(1 - (P*r/EMI)) / log(1+r)
        // Wait, standard formula: n = log(EMI / (EMI - P*r)) / log(1+r)

        BigDecimal emi = loan.getEmiAmount();
        BigDecimal monthlyRate = loan.getInterestRate().divide(BigDecimal.valueOf(1200), MC);

        BigDecimal pTimesR = newPrincipal.multiply(monthlyRate); // P*r

        // If P*r >= EMI, unlimited tenure (debt trap). Should check.
        if (pTimesR.compareTo(emi) >= 0) {
            Map<String, Object> result = new HashMap<>();
            result.put("error", "Prepayment insufficient to reduce tenure with current EMI");
            return result;
        }

        BigDecimal emiMinusPR = emi.subtract(pTimesR); // EMI - P*r
        BigDecimal ratio = emi.divide(emiMinusPR, MC); // EMI / (EMI - P*r)

        double logRatio = Math.log(ratio.doubleValue());
        double logOnePlusR = Math.log(monthlyRate.add(BigDecimal.ONE).doubleValue());

        double newTenure = logRatio / logOnePlusR;

        Map<String, Object> result = new HashMap<>();
        result.put("originalTenureMonths", loan.getTenureMonths());
        result.put("newTenureMonths", (int) Math.ceil(newTenure));
        result.put("savedInterest", "To be calculated"); // Placeholder for now

        return result;
    }
}
