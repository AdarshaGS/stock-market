package com.savings.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.savings.data.RecurringDeposit;
import com.savings.data.RecurringDepositDTO;
import com.savings.repo.RecurringDepositRepository;

@ExtendWith(MockitoExtension.class)
class RecurringDepositServiceImplTest {

    @Mock
    private RecurringDepositRepository repository;

    @InjectMocks
    private RecurringDepositServiceImpl service;

    private RecurringDeposit recurringDeposit;

    @BeforeEach
    void setUp() {
        recurringDeposit = RecurringDeposit.builder()
                .id(1L)
                .userId(1L)
                .bankName("HDFC")
                .accountNumber("RD123456")
                .monthlyInstallment(BigDecimal.valueOf(5000))
                .interestRate(BigDecimal.valueOf(6.5))
                .tenureMonths(12)
                .startDate(LocalDate.of(2025, 1, 1))
                .status("ACTIVE")
                .build();
    }

    @Test
    void testCreateRD_CalculatesMaturityCorrectly() {
        when(repository.save(any(RecurringDeposit.class))).thenReturn(recurringDeposit);

        RecurringDepositDTO result = service.createRecurringDeposit(recurringDeposit);

        assertNotNull(result);
        assertNotNull(result.getMaturityAmount());
        assertNotNull(result.getMaturityDate());
        assertEquals(LocalDate.of(2026, 1, 1), result.getMaturityDate());
        // Maturity should be more than total principal (5000 * 12 = 60000)
        assertTrue(result.getMaturityAmount().compareTo(BigDecimal.valueOf(60000)) > 0);
        verify(repository, times(1)).save(any(RecurringDeposit.class));
    }

    @Test
    void testGetRecurringDeposit_Success() {
        when(repository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(recurringDeposit));

        RecurringDepositDTO result = service.getRecurringDeposit(1L, 1L);

        assertNotNull(result);
        assertEquals("HDFC", result.getBankName());
        assertEquals(BigDecimal.valueOf(5000), result.getMonthlyInstallment());
    }

    @Test
    void testGetRecurringDeposit_NotFound_ThrowsException() {
        when(repository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.getRecurringDeposit(1L, 1L);
        });

        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void testGetAllRDs_ForUser() {
        RecurringDeposit rd2 = RecurringDeposit.builder()
                .id(2L)
                .userId(1L)
                .bankName("SBI")
                .monthlyInstallment(BigDecimal.valueOf(3000))
                .interestRate(BigDecimal.valueOf(7.0))
                .tenureMonths(24)
                .startDate(LocalDate.of(2025, 1, 1))
                .build();

        when(repository.findAllByUserId(1L)).thenReturn(Arrays.asList(recurringDeposit, rd2));

        List<RecurringDepositDTO> results = service.getAllRecurringDeposits(1L);

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("HDFC", results.get(0).getBankName());
        assertEquals("SBI", results.get(1).getBankName());
    }

    @Test
    void testUpdateRD_Success() {
        RecurringDeposit updatedRD = RecurringDeposit.builder()
                .bankName("ICICI")
                .monthlyInstallment(BigDecimal.valueOf(7000))
                .interestRate(BigDecimal.valueOf(7.5))
                .tenureMonths(18)
                .startDate(LocalDate.of(2025, 2, 1))
                .status("ACTIVE")
                .build();

        when(repository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(recurringDeposit));
        when(repository.save(any(RecurringDeposit.class))).thenReturn(recurringDeposit);

        RecurringDepositDTO result = service.updateRecurringDeposit(1L, 1L, updatedRD);

        assertNotNull(result);
        verify(repository, times(1)).save(any(RecurringDeposit.class));
    }

    @Test
    void testDeleteRD_Success() {
        when(repository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(recurringDeposit));
        doNothing().when(repository).delete(recurringDeposit);

        service.deleteRecurringDeposit(1L, 1L);

        verify(repository, times(1)).delete(recurringDeposit);
    }
}
