package com.savings.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.savings.data.RecurringDeposit;

public interface RecurringDepositRepository
        extends JpaRepository<RecurringDeposit, Long>, JpaSpecificationExecutor<RecurringDeposit> {

    List<RecurringDeposit> findAllByUserId(Long userId);

    Optional<RecurringDeposit> findByIdAndUserId(Long id, Long userId);
}
