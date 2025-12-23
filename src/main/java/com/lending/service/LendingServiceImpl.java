package com.lending.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lending.data.LendingDTO;
import com.lending.data.LendingRecord;
import com.lending.data.LendingStatus;
import com.lending.data.Repayment;
import com.lending.data.RepaymentDTO;
import com.lending.repo.LendingRepository;
import com.lending.repo.RepaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LendingServiceImpl implements LendingService {

    private final LendingRepository lendingRepository;
    private final RepaymentRepository repaymentRepository;

    @Override
    @Transactional
    public LendingDTO createLending(LendingDTO dto) {
        LendingRecord record = LendingRecord.builder()
                .userId(dto.getUserId())
                .borrowerName(dto.getBorrowerName())
                .borrowerContact(dto.getBorrowerContact())
                .amountLent(dto.getAmountLent())
                .amountRepaid(BigDecimal.ZERO)
                .outstandingAmount(dto.getAmountLent())
                .dateLent(dto.getDateLent())
                .dueDate(dto.getDueDate())
                .status(LendingStatus.PENDING)
                .notes(dto.getNotes())
                .build();

        return mapToDTO(lendingRepository.save(record));
    }

    @Override
    public List<LendingDTO> getUserLendings(Long userId) {
        return lendingRepository.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LendingDTO getLendingById(Long id) {
        return lendingRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Lending record not found with id: " + id));
    }

    @Override
    @Transactional
    public LendingDTO addRepayment(Long lendingId, RepaymentDTO repaymentDTO) {
        LendingRecord record = lendingRepository.findById(lendingId)
                .orElseThrow(() -> new RuntimeException("Lending record not found"));

        if (repaymentDTO.getAmount().compareTo(record.getOutstandingAmount()) > 0) {
            throw new RuntimeException("Repayment amount cannot exceed outstanding amount");
        }

        Repayment repayment = Repayment.builder()
                .lendingRecord(record)
                .amount(repaymentDTO.getAmount())
                .repaymentDate(repaymentDTO.getRepaymentDate())
                .repaymentMethod(repaymentDTO.getRepaymentMethod())
                .notes(repaymentDTO.getNotes())
                .build();

        repaymentRepository.save(repayment);

        // Update record
        BigDecimal newAmountRepaid = record.getAmountRepaid().add(repaymentDTO.getAmount());
        record.setAmountRepaid(newAmountRepaid);
        record.setOutstandingAmount(record.getAmountLent().subtract(newAmountRepaid));

        // Update status
        if (record.getOutstandingAmount().compareTo(BigDecimal.ZERO) == 0) {
            record.setStatus(LendingStatus.PAID);
        } else {
            record.setStatus(LendingStatus.PARTIALLY_PAID);
        }

        return mapToDTO(lendingRepository.save(record));
    }

    @Override
    @Transactional
    public LendingDTO closeLending(Long id) {
        LendingRecord record = lendingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lending record not found"));

        record.setAmountRepaid(record.getAmountLent());
        record.setOutstandingAmount(BigDecimal.ZERO);
        record.setStatus(LendingStatus.PAID);

        return mapToDTO(lendingRepository.save(record));
    }

    private LendingDTO mapToDTO(LendingRecord record) {
        return LendingDTO.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .borrowerName(record.getBorrowerName())
                .borrowerContact(record.getBorrowerContact())
                .amountLent(record.getAmountLent())
                .amountRepaid(record.getAmountRepaid())
                .outstandingAmount(record.getOutstandingAmount())
                .dateLent(record.getDateLent())
                .dueDate(record.getDueDate())
                .status(record.getStatus())
                .notes(record.getNotes())
                .repayments(record.getRepayments().stream()
                        .map(r -> RepaymentDTO.builder()
                                .id(r.getId())
                                .amount(r.getAmount())
                                .repaymentDate(r.getRepaymentDate())
                                .repaymentMethod(r.getRepaymentMethod())
                                .notes(r.getNotes())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
