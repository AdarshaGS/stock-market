package com.lending.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lending.data.Repayment;

@Repository
public interface RepaymentRepository extends JpaRepository<Repayment, Long> {
}
