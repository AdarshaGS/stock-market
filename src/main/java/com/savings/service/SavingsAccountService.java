package com.savings.service;

import java.util.List;

import com.savings.data.SavingsAccount;
import com.savings.data.SavingsAccountDTO;

public interface SavingsAccountService {

    SavingsAccountDTO createSavingsAccountDetails(SavingsAccount savingsAccount);

    SavingsAccountDTO retrieveSavingsAccountDetails(Long userId);

    List<SavingsAccountDTO> getAllSavingsAccounts(Long userId);

    SavingsAccountDTO updateSavingsAccount(Long id, Long userId, SavingsAccount savingsAccount);

    void deleteSavingsAccount(Long id, Long userId);

}
