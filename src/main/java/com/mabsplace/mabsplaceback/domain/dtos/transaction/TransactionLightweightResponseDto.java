package com.mabsplace.mabsplaceback.domain.dtos.transaction;

import com.mabsplace.mabsplaceback.domain.enums.TransactionStatus;
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
public class TransactionLightweightResponseDto implements Serializable {
    private Long id;
    private String transactionRef;
    private String senderName;
    private BigDecimal amount;
    private Date transactionDate;
    private String currencySymbol;
    private String transactionType;
    private TransactionStatus transactionStatus;
}
