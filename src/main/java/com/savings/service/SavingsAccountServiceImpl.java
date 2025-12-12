package com.savings.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.savings.data.SavingsAccount;
import com.savings.data.SavingsAccountDTO;
import com.savings.repo.SavingsAccountRepository;

@Service
public class SavingsAccountServiceImpl implements SavingsAccountService {

    @Autowired
    private final SavingsAccountRepository repository;

    public SavingsAccountServiceImpl(final SavingsAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public SavingsAccountDTO createSavingsAccountDetails(SavingsAccount savingsAccount) {
        try {
            this.repository.save(savingsAccount);
            return SavingsAccountDTO.builder().Id(savingsAccount.getId()).build();
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Savings account already exists for this user and bank combination");
        }
    }

    @Override
    public SavingsAccountDTO retrieveSavingsAccountDetails(Long userId) {
        SavingsAccount savingsAccount = this.repository.findOneByUserId(userId);

        if (savingsAccount == null) {
            throw new RuntimeException("Savings account not found for user ID: " + userId);
        }

        return convertToDTO(savingsAccount);
    }

    @Override
    public List<SavingsAccountDTO> getAllSavingsAccounts(Long userId) {
        return repository.findAllByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SavingsAccountDTO updateSavingsAccount(Long id, Long userId, SavingsAccount savingsAccount) {
        SavingsAccount existing = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Savings account not found for ID: " + id));

        // Update fields
        existing.setAccountHolderName(savingsAccount.getAccountHolderName());
        existing.setBankName(savingsAccount.getBankName());
        existing.setAmount(savingsAccount.getAmount());

        try {
            SavingsAccount updated = repository.save(existing);
            return convertToDTO(updated);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException(
                    "Cannot update: Savings account already exists for this user and bank combination");
        }
    }

    @Override
    public void deleteSavingsAccount(Long id, Long userId) {
        SavingsAccount account = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Savings account not found for ID: " + id));
        repository.delete(account);
    }

    private SavingsAccountDTO convertToDTO(SavingsAccount account) {
        return SavingsAccountDTO.builder()
                .Id(account.getId())
                .accountHolderName(account.getAccountHolderName())
                .bankName(account.getBankName())
                .amount(account.getAmount())
                .build();
    }

}
