package com.mabsplace.mabsplaceback.domain.dtos.expense;

import com.mabsplace.mabsplaceback.domain.enums.PaymentMethod;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ExpenseLightweightResponseDto implements Serializable {
    private Long id;
    private String categoryName;
    private BigDecimal amount;
    private String description;
    private LocalDateTime expenseDate;
    private PaymentMethod paymentMethod;
    private boolean isRecurring;
    private String currencySymbol;
    private Long createdById;
    private String createdByName;
}
