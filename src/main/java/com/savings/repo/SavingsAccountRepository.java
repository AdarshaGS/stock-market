package com.savings.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.savings.data.SavingsAccount;

public interface SavingsAccountRepository
        extends JpaRepository<SavingsAccount, Long>, JpaSpecificationExecutor<SavingsAccount> {

    SavingsAccount findOneByUserId(Long userId);

    List<SavingsAccount> findAllByUserId(Long userId);

    Optional<SavingsAccount> findByIdAndUserId(Long id, Long userId);

}
