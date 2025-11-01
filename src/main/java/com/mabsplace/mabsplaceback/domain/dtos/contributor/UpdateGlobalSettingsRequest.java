package com.mabsplace.mabsplaceback.domain.dtos.contributor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGlobalSettingsRequest {

    private BigDecimal minProfitThreshold;

    @NotNull(message = "Payment day of month is required")
    @Min(value = 1, message = "Payment day must be between 1 and 28")
    @Max(value = 28, message = "Payment day must be between 1 and 28")
    private Integer paymentDayOfMonth;

    @NotNull(message = "Is enabled flag is required")
    private Boolean isEnabled;
}
