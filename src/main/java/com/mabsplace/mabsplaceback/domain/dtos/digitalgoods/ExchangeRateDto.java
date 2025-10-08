package com.mabsplace.mabsplaceback.domain.dtos.digitalgoods;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ExchangeRateDto implements Serializable {
    private Long id;
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal rate;
    private Boolean isActive;
    private Date effectiveDate;
    private Date lastUpdated;
}
