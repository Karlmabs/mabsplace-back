package com.mabsplace.mabsplaceback.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "global_payment_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalPaymentSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(precision = 19, scale = 2)
    private BigDecimal minProfitThreshold;

    @Column(nullable = false)
    private Integer paymentDayOfMonth = 1; // Default to 1st of month (1-28)

    @Column(nullable = false)
    private Boolean isEnabled = true;

    private LocalDateTime lastPaymentRun;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
