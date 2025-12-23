package com.lending.service;

import java.util.List;

import com.lending.data.LendingDTO;
import com.lending.data.RepaymentDTO;

public interface LendingService {
    LendingDTO createLending(LendingDTO lendingDTO);

    List<LendingDTO> getUserLendings(Long userId);

    LendingDTO getLendingById(Long id);

    LendingDTO addRepayment(Long lendingId, RepaymentDTO repaymentDTO);

    LendingDTO closeLending(Long id);
}
