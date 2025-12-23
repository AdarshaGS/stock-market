package com.lending.data;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepaymentDTO {
    private Long id;
    private BigDecimal amount;
    @JsonFormat(pattern = "yyyy-M-d")
    private LocalDate repaymentDate;
    private RepaymentMethod repaymentMethod;
    private String notes;
}
