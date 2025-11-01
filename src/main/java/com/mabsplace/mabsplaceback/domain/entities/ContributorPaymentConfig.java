package com.mabsplace.mabsplaceback.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "contributor_payment_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContributorPaymentConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"wallet", "referrer", "referrals", "roles", "userProfile", "password", "tokens"})
    private User user;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AmountType amountType;

    @Column(precision = 19, scale = 2)
    private BigDecimal fixedAmount;

    @Column(precision = 5, scale = 2)
    private BigDecimal percentageValue;

    @Column(precision = 19, scale = 2)
    private BigDecimal minProfitThreshold;

    @Column(nullable = false)
    private Boolean useGlobalThreshold = false;

    @Column(nullable = false)
    private Boolean alwaysPay = false;

    @ManyToOne
    @JoinColumn(name = "currency_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Currency currency;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum AmountType {
        FIXED,
        PERCENTAGE
    }
}
