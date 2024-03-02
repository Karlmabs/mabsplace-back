package com.mabsplace.mabsplaceback.domain.dtos.promoCode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PromoCodeRequestDto {

    private String code;
    private LocalDateTime expirationDate;
    private int maxUsage;
    private int usedCount = 0;

    private Long ownerId;
}
