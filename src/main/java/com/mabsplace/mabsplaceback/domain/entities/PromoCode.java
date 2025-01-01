package com.mabsplace.mabsplaceback.domain.entities;

import com.mabsplace.mabsplaceback.domain.enums.PromoCodeStatus;
import jakarta.persistence.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promo_code")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromoCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    private LocalDateTime expirationDate;

    private int maxUsage;

    private int usedCount;

    @Column(nullable = false)
    private BigDecimal discountAmount; // Percentage discount

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PromoCodeStatus status = PromoCodeStatus.ACTIVE;

    // Remove owner-related fields since codes are public

    public boolean isValid() {
        return status == PromoCodeStatus.ACTIVE && !isExpired() && !isExhausted();
    }

    public boolean isExpired() {
        return expirationDate != null && LocalDateTime.now().isAfter(expirationDate);
    }

    public boolean isExhausted() {
        return usedCount >= maxUsage;
    }
}