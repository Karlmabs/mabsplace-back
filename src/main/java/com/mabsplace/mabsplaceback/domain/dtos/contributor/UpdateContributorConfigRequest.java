package com.mabsplace.mabsplaceback.domain.dtos.contributor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateContributorConfigRequest {

    @NotBlank(message = "Amount type is required (FIXED or PERCENTAGE)")
    private String amountType; // FIXED or PERCENTAGE

    private BigDecimal fixedAmount;

    private BigDecimal percentageValue;

    private BigDecimal minProfitThreshold;

    @NotNull(message = "Use global threshold flag is required")
    private Boolean useGlobalThreshold;

    @NotNull(message = "Always pay flag is required")
    private Boolean alwaysPay;

    @NotNull(message = "Currency ID is required")
    private Long currencyId;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotNull(message = "Is active flag is required")
    private Boolean isActive;
}
