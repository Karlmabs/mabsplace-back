package com.mabsplace.mabsplaceback.domain.dtos.expense;

import com.mabsplace.mabsplaceback.domain.enums.PaymentMethod;
import com.mabsplace.mabsplaceback.domain.enums.RecurrencePeriod;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ExpenseRequestDto {
    private Long categoryId;
    private BigDecimal amount;
    private String description;
    private LocalDateTime expenseDate;
    private PaymentMethod paymentMethod;
    private String receiptUrl;
    private boolean isRecurring;
    private RecurrencePeriod recurrencePeriod;
    private LocalDateTime nextRecurrenceDate;
    private Long currencyId;
}