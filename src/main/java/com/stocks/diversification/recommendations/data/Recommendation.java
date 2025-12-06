package com.stocks.diversification.recommendations.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "recommendations")
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Recommendation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "threshold_percentage")
    private Double thresholdPercentage;

    @Column(name = "recommendation_message", length = 2000)
    private String recommendationMessage;
}
