package com.mabsplace.mabsplaceback.domain.dtos.expense;

import com.mabsplace.mabsplaceback.domain.dtos.currency.CurrencyResponseDto;
import com.mabsplace.mabsplaceback.domain.dtos.expenseCategory.ExpenseCategoryResponseDto;
import com.mabsplace.mabsplaceback.domain.dtos.user.UserResponseDto;
import com.mabsplace.mabsplaceback.domain.enums.PaymentMethod;
import com.mabsplace.mabsplaceback.domain.enums.RecurrencePeriod;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ExpenseResponseDto {
    private Long id;
    private ExpenseCategoryResponseDto category;
    private BigDecimal amount;
    private String description;
    private LocalDateTime expenseDate;
    private PaymentMethod paymentMethod;
    private String receiptUrl;
    private boolean isRecurring;
    private RecurrencePeriod recurrencePeriod;
    private LocalDateTime nextRecurrenceDate;
    private CurrencyResponseDto currency;
    private UserResponseDto createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
