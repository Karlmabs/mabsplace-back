package com.mabsplace.mabsplaceback.domain.dtos.discount;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDiscountDTO {
    private Long id;
    private BigDecimal discountPercentage;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long serviceId;
    private boolean isGlobal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
