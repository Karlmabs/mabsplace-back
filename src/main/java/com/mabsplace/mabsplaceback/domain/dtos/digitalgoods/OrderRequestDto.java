package com.mabsplace.mabsplaceback.domain.dtos.digitalgoods;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OrderRequestDto implements Serializable {
    private Long userId;
    private Long productId;
    private BigDecimal amount; // Montant demandé (ex: 10, 25, 50)
}
