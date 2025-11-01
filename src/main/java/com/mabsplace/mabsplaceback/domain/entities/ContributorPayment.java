package com.mabsplace.mabsplaceback.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "contributor_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContributorPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "config_id", nullable = false)
    @JsonIgnoreProperties({"user"})
    private ContributorPaymentConfig config;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"wallet", "referrer", "referrals", "roles", "userProfile", "password", "tokens"})
    private User user;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amountPaid;

    @Column(nullable = false, length = 7)
    private String paymentPeriod; // Format: YYYY-MM

    @Column(precision = 19, scale = 2)
    private BigDecimal netProfitAtTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(length = 100)
    private String coolpayTransactionRef;

    @Column(columnDefinition = "TEXT")
    private String coolpayResponse;

    @Column(columnDefinition = "TEXT")
    private String failureReason;

    private LocalDateTime processedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum PaymentStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }
}
