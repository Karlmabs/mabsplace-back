package com.mabsplace.mabsplaceback.domain.entities;

import com.mabsplace.mabsplaceback.domain.enums.PaymentMethod;
import com.mabsplace.mabsplaceback.domain.enums.RecurrencePeriod;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenses")
@Data
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private ExpenseCategory category;

    private BigDecimal amount;
    private String description;
    private LocalDateTime expenseDate;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private String receiptUrl;
    private boolean isRecurring;

    @Enumerated(EnumType.STRING)
    private RecurrencePeriod recurrencePeriod;

    private LocalDateTime nextRecurrenceDate;

    @ManyToOne
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
