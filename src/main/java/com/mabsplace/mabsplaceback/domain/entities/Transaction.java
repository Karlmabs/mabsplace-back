package com.mabsplace.mabsplaceback.domain.entities;

import com.mabsplace.mabsplaceback.domain.enums.TransactionStatus;
import com.mabsplace.mabsplaceback.domain.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "transactions")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Transaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "sender_wallet_id", referencedColumnName = "id")
  private Wallet senderWallet;

  @ManyToOne
  @JoinColumn(name = "receiver_wallet_id", referencedColumnName = "id")
  private Wallet receiverWallet;

  private String senderName;

  private String senderPhoneNumber;

  private BigDecimal amount;

  private Date transactionDate;

  @ManyToOne
  @JoinColumn(name = "currency_id", referencedColumnName = "id")
  private Currency currency;

  @Enumerated(EnumType.STRING)
  private TransactionType transactionType;

  @Enumerated(EnumType.STRING)
  private TransactionStatus transactionStatus;

}
