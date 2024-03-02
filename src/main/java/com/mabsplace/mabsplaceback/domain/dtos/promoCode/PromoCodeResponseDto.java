package com.mabsplace.mabsplaceback.domain.dtos.promoCode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PromoCodeResponseDto {

    private Long id;
    private String code;
    private String expirationDate;
    private int maxUsage;
    private int usedCount;
    private Long ownerId;

}
