package com.investments.stocks.networth.data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
@Table(name = "user_assets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false)
    private AssetType assetType;

    @Column(name = "reference_symbol")
    private String referenceSymbol;

    @Column(nullable = false)
    private String name;

    private BigDecimal quantity;

    @Column(name = "current_value", nullable = false)
    private BigDecimal currentValue;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    public enum AssetType {
        STOCK, MUTUAL_FUND, ETF, SAVINGS, PF, GOLD, CASH, LENDING, OTHER
    }
}
