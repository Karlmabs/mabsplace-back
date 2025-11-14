package com.mabsplace.mabsplaceback.domain.entities;

import com.mabsplace.mabsplaceback.domain.enums.WithdrawalOperator;
import com.mabsplace.mabsplaceback.domain.enums.WithdrawalStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "withdrawals")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Withdrawal {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private BigDecimal amount;

  @ManyToOne
  @JoinColumn(name = "currency_id", referencedColumnName = "id")
  private Currency currency;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private WithdrawalOperator transactionOperator;

  @Column(nullable = false)
  private String customerName;

  private String customerPhoneNumber;

  private String customerEmail;

  @Column(length = 2)
  private String customerLang; // en or fr

  private String customerUsername; // For MCP withdrawals

  @Column(columnDefinition = "TEXT")
  private String transactionReason;

  @Column(nullable = false, unique = true)
  private String appTransactionRef;

  private String coolpayTransactionRef; // Reference from My-CoolPay API response

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private WithdrawalStatus status;

  @ManyToOne
  @JoinColumn(name = "created_by_user_id", referencedColumnName = "id")
  private User createdBy;

  @Column(columnDefinition = "TEXT")
  private String errorMessage; // Store error details if withdrawal fails

  @CreationTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  @Column(updatable = false)
  private Date createdAt;

  @UpdateTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  private Date updatedAt;
}
