package com.mabsplace.mabsplaceback.domain.dtos.discount;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DiscountResponseDto {
    private Long id;
    private double amount;
    private String expirationDate;
    private Long userId;
}
