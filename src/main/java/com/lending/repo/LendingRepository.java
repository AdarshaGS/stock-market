package com.lending.repo;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lending.data.LendingRecord;
import com.lending.data.LendingStatus;

@Repository
public interface LendingRepository extends JpaRepository<LendingRecord, Long> {
    List<LendingRecord> findByUserId(Long userId);

    List<LendingRecord> findByStatusNotAndDueDateBefore(LendingStatus status, LocalDate date);

    List<LendingRecord> findByStatusNotAndDueDate(LendingStatus status, LocalDate date);
}
