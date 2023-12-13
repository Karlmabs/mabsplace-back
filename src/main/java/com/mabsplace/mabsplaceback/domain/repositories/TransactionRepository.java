package com.mabsplace.mabsplaceback.domain.repositories;

import com.mabsplace.mabsplaceback.domain.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
