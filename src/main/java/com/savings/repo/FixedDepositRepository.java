package com.savings.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.savings.data.FixedDeposit;

public interface FixedDepositRepository
        extends JpaRepository<FixedDeposit, Long>, JpaSpecificationExecutor<FixedDeposit> {

    List<FixedDeposit> findAllByUserId(Long userId);

    Optional<FixedDeposit> findByIdAndUserId(Long id, Long userId);
}
