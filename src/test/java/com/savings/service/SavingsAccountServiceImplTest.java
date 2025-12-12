package com.savings.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.savings.data.SavingsAccount;
import com.savings.data.SavingsAccountDTO;
import com.savings.repo.SavingsAccountRepository;

@ExtendWith(MockitoExtension.class)
class SavingsAccountServiceImplTest {

    @Mock
    private SavingsAccountRepository repository;

    @InjectMocks
    private SavingsAccountServiceImpl service;

    private SavingsAccount savingsAccount;

    @BeforeEach
    void setUp() {
        savingsAccount = SavingsAccount.builder()
                .Id(1L)
                .userId(1L)
                .accountHolderName("Test User")
                .bankName("SBI")
                .amount(BigDecimal.valueOf(10000))
                .build();
    }

    @Test
    void testCreateSavingsAccount_Success() {
        when(repository.save(any(SavingsAccount.class))).thenReturn(savingsAccount);

        SavingsAccountDTO result = service.createSavingsAccountDetails(savingsAccount);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(repository, times(1)).save(savingsAccount);
    }

    @Test
    void testCreateSavingsAccount_DuplicateThrowsException() {
        when(repository.save(any(SavingsAccount.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.createSavingsAccountDetails(savingsAccount);
        });

        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    void testRetrieveSavingsAccount_Success() {
        when(repository.findOneByUserId(1L)).thenReturn(savingsAccount);

        SavingsAccountDTO result = service.retrieveSavingsAccountDetails(1L);

        assertNotNull(result);
        assertEquals("Test User", result.getAccountHolderName());
        assertEquals("SBI", result.getBankName());
        assertEquals(BigDecimal.valueOf(10000), result.getAmount());
    }

    @Test
    void testRetrieveSavingsAccount_NotFound_ThrowsException() {
        when(repository.findOneByUserId(1L)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.retrieveSavingsAccountDetails(1L);
        });

        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void testGetAllSavingsAccounts_ReturnsMultiple() {
        SavingsAccount account2 = SavingsAccount.builder()
                .Id(2L)
                .userId(1L)
                .accountHolderName("Test User")
                .bankName("HDFC")
                .amount(BigDecimal.valueOf(20000))
                .build();

        when(repository.findAllByUserId(1L)).thenReturn(Arrays.asList(savingsAccount, account2));

        List<SavingsAccountDTO> results = service.getAllSavingsAccounts(1L);

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("SBI", results.get(0).getBankName());
        assertEquals("HDFC", results.get(1).getBankName());
    }

    @Test
    void testUpdateSavingsAccount_Success() {
        SavingsAccount updatedAccount = SavingsAccount.builder()
                .accountHolderName("Updated Name")
                .bankName("ICICI")
                .amount(BigDecimal.valueOf(15000))
                .build();

        when(repository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(savingsAccount));
        when(repository.save(any(SavingsAccount.class))).thenReturn(savingsAccount);

        SavingsAccountDTO result = service.updateSavingsAccount(1L, 1L, updatedAccount);

        assertNotNull(result);
        verify(repository, times(1)).save(any(SavingsAccount.class));
    }

    @Test
    void testUpdateSavingsAccount_NotFound_ThrowsException() {
        when(repository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        SavingsAccount updatedAccount = SavingsAccount.builder().build();

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.updateSavingsAccount(1L, 1L, updatedAccount);
        });

        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void testDeleteSavingsAccount_Success() {
        when(repository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(savingsAccount));
        doNothing().when(repository).delete(savingsAccount);

        service.deleteSavingsAccount(1L, 1L);

        verify(repository, times(1)).delete(savingsAccount);
    }

    @Test
    void testDeleteSavingsAccount_NotFound_ThrowsException() {
        when(repository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.deleteSavingsAccount(1L, 1L);
        });

        assertTrue(exception.getMessage().contains("not found"));
    }
}
