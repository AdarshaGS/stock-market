package com.investments.mutualfunds.data;

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
public class MFTransaction {
    private String txnId;
    private LocalDate txnDate;
    private String txnType; // BUY, SELL, SIP, etc.
    private BigDecimal amount;
    private BigDecimal units;
    private BigDecimal nav;
}
