package com.protection.insurance.data;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "insurance_policies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Insurance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InsuranceType type;

    private String policyNumber;
    private String provider;

    private BigDecimal premiumAmount;
    private BigDecimal coverAmount;

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextPremiumDate;

    // Optional: For partial fetch from Account Aggregator
    private boolean isAutoFetched;
}
