package com.savings.data;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecurringDepositDTO {
    private Long id;
    private Long userId;
    private String bankName;
    private String accountNumber;
    private BigDecimal monthlyInstallment;
    private BigDecimal interestRate;
    private Integer tenureMonths;
    private BigDecimal maturityAmount;
    private LocalDate startDate;
    private LocalDate maturityDate;
    private String status;
}
