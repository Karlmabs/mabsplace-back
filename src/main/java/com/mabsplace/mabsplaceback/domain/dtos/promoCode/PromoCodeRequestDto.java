package com.mabsplace.mabsplaceback.domain.dtos.promoCode;

import com.mabsplace.mabsplaceback.domain.enums.PromoCodeStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeRequestDto {
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    @NotNull
    private BigDecimal discountAmount;

    @NotNull
    private LocalDateTime expirationDate;

    @NotNull
    @Min(1)
    private Integer maxUsage;

    @NotNull
    private PromoCodeStatus status;

    // Optional: number of codes to generate
    @Min(1)
    @Max(100)
    private Integer quantity = 1;
}
