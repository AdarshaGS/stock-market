package com.savings.service;

import java.util.List;

import com.savings.data.RecurringDeposit;
import com.savings.data.RecurringDepositDTO;

public interface RecurringDepositService {

    RecurringDepositDTO createRecurringDeposit(RecurringDeposit recurringDeposit);

    RecurringDepositDTO getRecurringDeposit(Long id, Long userId);

    List<RecurringDepositDTO> getAllRecurringDeposits(Long userId);

    RecurringDepositDTO updateRecurringDeposit(Long id, Long userId, RecurringDeposit recurringDeposit);

    void deleteRecurringDeposit(Long id, Long userId);
}
