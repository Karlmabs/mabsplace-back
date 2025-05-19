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

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User assignedUser;

    // Check if the promo code is assigned to the given user
    // Returns true if:
    // 1. The promo code is not assigned to any user (public promo code)
    // 2. OR if the user is not null and has the same ID as the assigned user
    public boolean isAssignedToUser(User user) {
        // If no user is assigned to this promo code, it's a public promo code
        if (assignedUser == null) {
            return true;
        }

        // If user is null, they can't use a user-specific promo code
        if (user == null) {
            return false;
        }

        // Compare by ID to avoid issues with different User object instances
        return user.getId() != null &&
               assignedUser.getId() != null &&
               user.getId().equals(assignedUser.getId());
    }

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