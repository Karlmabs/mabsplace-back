package com.mabsplace.mabsplaceback.domain.dtos.promoCode;

import com.mabsplace.mabsplaceback.domain.enums.PromoCodeStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeResponseDto {
    private Long id;
    private String code;
    private LocalDateTime expirationDate;
    private int maxUsage;
    private int usedCount;
    private BigDecimal discountAmount;
    private PromoCodeStatus status;
    private boolean isValid;
    private boolean isExpired;
    private boolean isExhausted;
    private long userId;
}
