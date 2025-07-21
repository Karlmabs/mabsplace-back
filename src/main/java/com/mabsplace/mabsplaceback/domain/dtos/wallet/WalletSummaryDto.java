package com.mabsplace.mabsplaceback.domain.dtos.wallet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class WalletSummaryDto implements Serializable {
    private Long id;
    private BigDecimal balance;
    private String currencySymbol;
}
