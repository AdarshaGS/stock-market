package com.savings.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.savings.data.RecurringDeposit;
import com.savings.data.RecurringDepositDTO;
import com.savings.repo.RecurringDepositRepository;

@Service
public class RecurringDepositServiceImpl implements RecurringDepositService {

    private final RecurringDepositRepository repository;

    public RecurringDepositServiceImpl(RecurringDepositRepository repository) {
        this.repository = repository;
    }

    @Override
    public RecurringDepositDTO createRecurringDeposit(RecurringDeposit recurringDeposit) {
        // Calculate maturity date
        LocalDate maturityDate = recurringDeposit.getStartDate().plusMonths(recurringDeposit.getTenureMonths());
        recurringDeposit.setMaturityDate(maturityDate);

        // Calculate maturity amount for RD using simplified formula
        // M = P * n * [1 + (n+1) * r / (2 * 12)]
        BigDecimal monthlyInstallment = recurringDeposit.getMonthlyInstallment();
        BigDecimal rate = recurringDeposit.getInterestRate().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        int months = recurringDeposit.getTenureMonths();

        // Quarterly compounding for RD
        double r = rate.doubleValue() / 4; // quarterly rate

        // Calculate maturity using RD formula
        double maturityValue = monthlyInstallment.doubleValue() * months *
                (1 + ((months + 1) * r / (2 * 3)));

        BigDecimal maturityAmount = BigDecimal.valueOf(maturityValue)
                .setScale(2, RoundingMode.HALF_UP);

        recurringDeposit.setMaturityAmount(maturityAmount);
        recurringDeposit.setStatus("ACTIVE");

        RecurringDeposit saved = repository.save(recurringDeposit);
        return convertToDTO(saved);
    }

    @Override
    public RecurringDepositDTO getRecurringDeposit(Long id, Long userId) {
        RecurringDeposit rd = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Recurring Deposit not found for ID: " + id));
        return convertToDTO(rd);
    }

    @Override
    public List<RecurringDepositDTO> getAllRecurringDeposits(Long userId) {
        return repository.findAllByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RecurringDepositDTO updateRecurringDeposit(Long id, Long userId, RecurringDeposit recurringDeposit) {
        RecurringDeposit existing = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Recurring Deposit not found for ID: " + id));

        // Update fields
        existing.setBankName(recurringDeposit.getBankName());
        existing.setAccountNumber(recurringDeposit.getAccountNumber());
        existing.setMonthlyInstallment(recurringDeposit.getMonthlyInstallment());
        existing.setInterestRate(recurringDeposit.getInterestRate());
        existing.setTenureMonths(recurringDeposit.getTenureMonths());
        existing.setStartDate(recurringDeposit.getStartDate());
        existing.setStatus(recurringDeposit.getStatus());

        // Recalculate maturity date and amount
        LocalDate maturityDate = existing.getStartDate().plusMonths(existing.getTenureMonths());
        existing.setMaturityDate(maturityDate);

        BigDecimal monthlyInstallment = existing.getMonthlyInstallment();
        BigDecimal rate = existing.getInterestRate().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        int months = existing.getTenureMonths();

        double r = rate.doubleValue() / 4;
        double maturityValue = monthlyInstallment.doubleValue() * months *
                (1 + ((months + 1) * r / (2 * 3)));

        existing.setMaturityAmount(BigDecimal.valueOf(maturityValue)
                .setScale(2, RoundingMode.HALF_UP));

        RecurringDeposit updated = repository.save(existing);
        return convertToDTO(updated);
    }

    @Override
    public void deleteRecurringDeposit(Long id, Long userId) {
        RecurringDeposit rd = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Recurring Deposit not found for ID: " + id));
        repository.delete(rd);
    }

    private RecurringDepositDTO convertToDTO(RecurringDeposit rd) {
        return RecurringDepositDTO.builder()
                .id(rd.getId())
                .userId(rd.getUserId())
                .bankName(rd.getBankName())
                .accountNumber(rd.getAccountNumber())
                .monthlyInstallment(rd.getMonthlyInstallment())
                .interestRate(rd.getInterestRate())
                .tenureMonths(rd.getTenureMonths())
                .maturityAmount(rd.getMaturityAmount())
                .startDate(rd.getStartDate())
                .maturityDate(rd.getMaturityDate())
                .status(rd.getStatus())
                .build();
    }
}
