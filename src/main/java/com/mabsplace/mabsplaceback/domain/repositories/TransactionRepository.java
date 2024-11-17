package com.mabsplace.mabsplaceback.domain.repositories;

import com.mabsplace.mabsplaceback.domain.entities.Transaction;
import com.mabsplace.mabsplaceback.domain.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByTransactionRef(String transactionRef);

    List<Transaction> findByTransactionStatusAndTransactionDateBefore(TransactionStatus transactionStatus, Date oneHourAgo);

    List<Transaction> findByReceiverWalletUserId(Long userId);
}
