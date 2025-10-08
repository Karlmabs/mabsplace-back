package com.mabsplace.mabsplaceback.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "exchange_rates", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"from_currency", "to_currency"})
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_currency", nullable = false, length = 3)
    private String fromCurrency; // EUR, USD

    @Column(name = "to_currency", nullable = false, length = 3)
    private String toCurrency; // XAF

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal rate; // 655.957000

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "effective_date")
    private java.util.Date effectiveDate;

    @Column(name = "last_updated")
    private java.util.Date lastUpdated;

    @PrePersist
    protected void onCreate() {
        effectiveDate = new java.util.Date();
        lastUpdated = new java.util.Date();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = new java.util.Date();
    }
}
