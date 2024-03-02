package com.mabsplace.mabsplaceback.domain.dtos.discount;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DiscountRequestDto {
    private double amount;
    private Long userId;
    private String expirationDate;
}
