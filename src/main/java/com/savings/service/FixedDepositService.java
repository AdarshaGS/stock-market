package com.savings.service;

import java.util.List;

import com.savings.data.FixedDeposit;
import com.savings.data.FixedDepositDTO;

public interface FixedDepositService {

    FixedDepositDTO createFixedDeposit(FixedDeposit fixedDeposit);

    FixedDepositDTO getFixedDeposit(Long id, Long userId);

    List<FixedDepositDTO> getAllFixedDeposits(Long userId);

    FixedDepositDTO updateFixedDeposit(Long id, Long userId, FixedDeposit fixedDeposit);

    void deleteFixedDeposit(Long id, Long userId);
}
