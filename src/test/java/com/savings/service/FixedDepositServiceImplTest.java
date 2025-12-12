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

import com.savings.data.FixedDeposit;
import com.savings.data.FixedDepositDTO;
import com.savings.repo.FixedDepositRepository;

@ExtendWith(MockitoExtension.class)
class FixedDepositServiceImplTest {

    @Mock
    private FixedDepositRepository repository;

    @InjectMocks
    private FixedDepositServiceImpl service;

    private FixedDeposit fixedDeposit;

    @BeforeEach
    void setUp() {
        fixedDeposit = FixedDeposit.builder()
                .id(1L)
                .userId(1L)
                .bankName("HDFC")
                .accountNumber("FD123456")
                .principalAmount(BigDecimal.valueOf(100000))
                .interestRate(BigDecimal.valueOf(6.5))
                .tenureMonths(12)
                .startDate(LocalDate.of(2025, 1, 1))
                .status("ACTIVE")
                .build();
    }

    @Test
    void testCreateFD_CalculatesMaturityCorrectly() {
        when(repository.save(any(FixedDeposit.class))).thenReturn(fixedDeposit);

        FixedDepositDTO result = service.createFixedDeposit(fixedDeposit);

        assertNotNull(result);
        assertNotNull(result.getMaturityAmount());
        assertNotNull(result.getMaturityDate());
        assertEquals(LocalDate.of(2026, 1, 1), result.getMaturityDate());
        assertTrue(result.getMaturityAmount().compareTo(BigDecimal.valueOf(100000)) > 0);
        verify(repository, times(1)).save(any(FixedDeposit.class));
    }

    @Test
    void testGetFixedDeposit_Success() {
        when(repository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(fixedDeposit));

        FixedDepositDTO result = service.getFixedDeposit(1L, 1L);

        assertNotNull(result);
        assertEquals("HDFC", result.getBankName());
        assertEquals(BigDecimal.valueOf(100000), result.getPrincipalAmount());
    }

    @Test
    void testGetFixedDeposit_NotFound_ThrowsException() {
        when(repository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.getFixedDeposit(1L, 1L);
        });

        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void testGetAllFDs_ForUser() {
        FixedDeposit fd2 = FixedDeposit.builder()
                .id(2L)
                .userId(1L)
                .bankName("SBI")
                .principalAmount(BigDecimal.valueOf(50000))
                .interestRate(BigDecimal.valueOf(7.0))
                .tenureMonths(24)
                .startDate(LocalDate.of(2025, 1, 1))
                .build();

        when(repository.findAllByUserId(1L)).thenReturn(Arrays.asList(fixedDeposit, fd2));

        List<FixedDepositDTO> results = service.getAllFixedDeposits(1L);

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("HDFC", results.get(0).getBankName());
        assertEquals("SBI", results.get(1).getBankName());
    }

    @Test
    void testUpdateFD_Success() {
        FixedDeposit updatedFD = FixedDeposit.builder()
                .bankName("ICICI")
                .principalAmount(BigDecimal.valueOf(150000))
                .interestRate(BigDecimal.valueOf(7.5))
                .tenureMonths(18)
                .startDate(LocalDate.of(2025, 2, 1))
                .status("ACTIVE")
                .build();

        when(repository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(fixedDeposit));
        when(repository.save(any(FixedDeposit.class))).thenReturn(fixedDeposit);

        FixedDepositDTO result = service.updateFixedDeposit(1L, 1L, updatedFD);

        assertNotNull(result);
        verify(repository, times(1)).save(any(FixedDeposit.class));
    }

    @Test
    void testDeleteFD_Success() {
        when(repository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(fixedDeposit));
        doNothing().when(repository).delete(fixedDeposit);

        service.deleteFixedDeposit(1L, 1L);

        verify(repository, times(1)).delete(fixedDeposit);
    }
}
