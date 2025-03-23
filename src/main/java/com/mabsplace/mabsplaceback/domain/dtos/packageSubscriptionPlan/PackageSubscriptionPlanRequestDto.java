package com.mabsplace.mabsplaceback.domain.dtos.packageSubscriptionPlan;

import com.mabsplace.mabsplaceback.domain.enums.Period;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PackageSubscriptionPlanRequestDto {
    private String name;
    private BigDecimal price;
    private Period period;
    private Long currencyId;
    private String description;
    private Long packageId;
    private boolean active;
}
