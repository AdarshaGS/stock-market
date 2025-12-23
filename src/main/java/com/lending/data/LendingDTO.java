package com.lending.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LendingDTO {
    private Long id;
    private Long userId;
    private String borrowerName;
    private String borrowerContact;
    private BigDecimal amountLent;
    private BigDecimal amountRepaid;
    private BigDecimal outstandingAmount;
    @JsonFormat(pattern = "yyyy-M-d")
    private LocalDate dateLent;
    @JsonFormat(pattern = "yyyy-M-d")
    private LocalDate dueDate;
    private LendingStatus status;
    private String notes;
    private List<RepaymentDTO> repayments;
}
