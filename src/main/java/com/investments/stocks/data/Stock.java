package com.investments.stocks.data;

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

import com.common.data.EntityType;
import com.common.data.TypedEntity;

@Table(name = "stocks")
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Stock implements TypedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type")
    @Builder.Default
    private EntityType entityType = EntityType.STOCK;

    @Column(name = "symbol", unique = true)
    private String symbol;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "price", nullable = true)
    private Double price;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "sector_id")
    private Long sectorId;

    @Column(name = "market_cap")
    private Double marketCap;

}
