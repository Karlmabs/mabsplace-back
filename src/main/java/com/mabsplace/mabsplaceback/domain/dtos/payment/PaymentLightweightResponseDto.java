package com.mabsplace.mabsplaceback.domain.dtos.payment;

import com.mabsplace.mabsplaceback.domain.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PaymentLightweightResponseDto implements Serializable {
    private Long id;
    private String userName;
    private BigDecimal amount;
    private Date paymentDate;
    private String currencySymbol;
    private String serviceName;
    private PaymentStatus status;
}
