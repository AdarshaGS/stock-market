package com.lending.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.lending.data.LendingRecord;
import com.lending.data.LendingStatus;
import com.lending.repo.LendingRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LendingDueDateScheduler {

    private static final Logger logger = LoggerFactory.getLogger(LendingDueDateScheduler.class);

    private final LendingRepository lendingRepository;

    // Run every day at 10:00 AM
    @Scheduled(cron = "0 0 10 * * ?")
    public void checkLendingDueDates() {
        logger.info("Starting Lending Due Date Check Job...");

        LocalDate today = LocalDate.now();

        // 1. Check for overdue lendings (Status != PAID and DueDate < Today)
        List<LendingRecord> overdueRecords = lendingRepository.findByStatusNotAndDueDateBefore(LendingStatus.PAID,
                today);
        processOverdueRecords(overdueRecords);

        // 2. Check for lendings due today (Status != PAID and DueDate == Today)
        List<LendingRecord> dueTodayRecords = lendingRepository.findByStatusNotAndDueDate(LendingStatus.PAID, today);
        processDueTodayRecords(dueTodayRecords);

        logger.info("Completed Lending Due Date Check Job.");
    }

    private void processOverdueRecords(List<LendingRecord> records) {
        if (records.isEmpty()) {
            logger.info("No overdue lending records found.");
            return;
        }

        logger.warn("Found {} overdue lending records:", records.size());
        for (LendingRecord record : records) {
            // In a real application, triggering an email or push notification would happen
            // here
            logger.warn("OVERDUE: Borrower '{}' owes {} (Outstanding: {}). Due Date was: {}",
                    record.getBorrowerName(),
                    record.getAmountLent(),
                    record.getOutstandingAmount(),
                    record.getDueDate());
        }
    }

    private void processDueTodayRecords(List<LendingRecord> records) {
        if (records.isEmpty()) {
            logger.info("No lending records due today.");
            return;
        }

        logger.info("Found {} lending records due today:", records.size());
        for (LendingRecord record : records) {
            // Trigger notification
            logger.info("DUE TODAY: Borrower '{}' owes {} (Outstanding: {}).",
                    record.getBorrowerName(),
                    record.getAmountLent(),
                    record.getOutstandingAmount());
        }
    }
}
