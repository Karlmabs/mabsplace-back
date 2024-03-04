package com.mabsplace.mabsplaceback.domain.dtos.transaction;

import com.mabsplace.mabsplaceback.domain.dtos.currency.CurrencyResponseDto;
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
public class TransactionResponseDto implements Serializable {
    private Long id;

    private long senderWalletId;

    private long receiverWalletId;

    private String senderName;

    private String senderPhoneNumber;

    private BigDecimal amount;
    private Date transactionDate;
    private CurrencyResponseDto currency;

    private String transactionType;

    private TransactionStatus transactionStatus;
}
