package com.savings.data;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SavingsAccountDTO {
    private Long Id;
    private String accountHolderName;
    private String bankName;
    private BigDecimal amount;
}
