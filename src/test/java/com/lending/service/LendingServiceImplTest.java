package com.lending.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.lending.data.LendingDTO;
import com.lending.data.LendingRecord;
import com.lending.data.LendingStatus;
import com.lending.data.Repayment;
import com.lending.data.RepaymentDTO;
import com.lending.data.RepaymentMethod;
import com.lending.repo.LendingRepository;
import com.lending.repo.RepaymentRepository;

public class LendingServiceImplTest {

    private LendingRepository lendingRepository;
    private RepaymentRepository repaymentRepository;
    private LendingServiceImpl lendingService;

    @BeforeEach
    void setUp() {
        lendingRepository = mock(LendingRepository.class);
        repaymentRepository = mock(RepaymentRepository.class);
        lendingService = new LendingServiceImpl(lendingRepository, repaymentRepository);
    }

    @Test
    void testCreateLending_InitializesAsPending() {
        LendingDTO dto = LendingDTO.builder()
                .userId(1L)
                .borrowerName("John Doe")
                .amountLent(new BigDecimal("1000"))
                .dateLent(LocalDate.now())
                .build();

        when(lendingRepository.save(any(LendingRecord.class))).thenAnswer(i -> i.getArguments()[0]);

        LendingDTO result = lendingService.createLending(dto);

        assertEquals(LendingStatus.PENDING, result.getStatus());
        assertEquals(new BigDecimal("1000"), result.getOutstandingAmount());
        assertEquals(BigDecimal.ZERO, result.getAmountRepaid());
    }

    @Test
    void testAddRepayment_UpdatesStatusAndOutstanding() {
        Long lendingId = 1L;
        LendingRecord record = LendingRecord.builder()
                .id(lendingId)
                .amountLent(new BigDecimal("1000"))
                .amountRepaid(BigDecimal.ZERO)
                .outstandingAmount(new BigDecimal("1000"))
                .status(LendingStatus.PENDING)
                .build();

        when(lendingRepository.findById(lendingId)).thenReturn(Optional.of(record));
        when(lendingRepository.save(any(LendingRecord.class))).thenAnswer(i -> i.getArguments()[0]);

        RepaymentDTO repaymentDto = RepaymentDTO.builder()
                .amount(new BigDecimal("400"))
                .repaymentDate(LocalDate.now())
                .repaymentMethod(RepaymentMethod.UPI)
                .build();

        LendingDTO result = lendingService.addRepayment(lendingId, repaymentDto);

        assertEquals(LendingStatus.PARTIALLY_PAID, result.getStatus());
        assertEquals(new BigDecimal("600"), result.getOutstandingAmount());
        assertEquals(new BigDecimal("400"), result.getAmountRepaid());
        verify(repaymentRepository, times(1)).save(any(Repayment.class));
    }

    @Test
    void testAddRepayment_FullyPaid() {
        Long lendingId = 1L;
        LendingRecord record = LendingRecord.builder()
                .id(lendingId)
                .amountLent(new BigDecimal("1000"))
                .amountRepaid(new BigDecimal("400"))
                .outstandingAmount(new BigDecimal("600"))
                .status(LendingStatus.PARTIALLY_PAID)
                .build();

        when(lendingRepository.findById(lendingId)).thenReturn(Optional.of(record));
        when(lendingRepository.save(any(LendingRecord.class))).thenAnswer(i -> i.getArguments()[0]);

        RepaymentDTO repaymentDto = RepaymentDTO.builder()
                .amount(new BigDecimal("600"))
                .repaymentDate(LocalDate.now())
                .repaymentMethod(RepaymentMethod.CASH)
                .build();

        LendingDTO result = lendingService.addRepayment(lendingId, repaymentDto);

        assertEquals(LendingStatus.PAID, result.getStatus());
        assertEquals(BigDecimal.ZERO, result.getOutstandingAmount());
        assertEquals(new BigDecimal("1000"), result.getAmountRepaid());
    }

    @Test
    void testAddRepayment_ExceedingOutstanding_ThrowsException() {
        Long lendingId = 1L;
        LendingRecord record = LendingRecord.builder()
                .id(lendingId)
                .outstandingAmount(new BigDecimal("100"))
                .build();

        when(lendingRepository.findById(lendingId)).thenReturn(Optional.of(record));

        RepaymentDTO repaymentDto = RepaymentDTO.builder()
                .amount(new BigDecimal("150"))
                .build();

        assertThrows(RuntimeException.class, () -> lendingService.addRepayment(lendingId, repaymentDto));
    }
}
