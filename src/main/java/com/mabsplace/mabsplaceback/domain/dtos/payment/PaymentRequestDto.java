package com.mabsplace.mabsplaceback.domain.dtos.payment;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PaymentRequestDto {
    private long userId;
    private BigDecimal amount;
    private Date paymentDate;
    private long currencyId;
    private long serviceId;
    private long servicePackageId;
    private String promoCode;
    private long subscriptionPlanId;
}
