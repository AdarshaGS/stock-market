package com.savings.data;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FixedDepositDTO {
    private Long id;
    private Long userId;
    private String bankName;
    private String accountNumber;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private Integer tenureMonths;
    private BigDecimal maturityAmount;
    @JsonFormat(pattern = "yyyy-M-d")
    private LocalDate startDate;
    @JsonFormat(pattern = "yyyy-M-d")
    private LocalDate maturityDate;
    private String status;
}
