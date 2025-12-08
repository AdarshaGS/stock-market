package com.stocks.networth.data;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_liabilities")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLiability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "liability_type", nullable = false)
    private LiabilityType liabilityType;

    @Column(nullable = false)
    private String name;

    @Column(name = "outstanding_amount", nullable = false)
    private BigDecimal outstandingAmount;

    @Column(name = "interest_rate")
    private BigDecimal interestRate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    public enum LiabilityType {
        HOME_LOAN, PERSONAL_LOAN, CAR_LOAN, EDUCATION_LOAN, CREDIT_CARD, OTHER
    }
}
