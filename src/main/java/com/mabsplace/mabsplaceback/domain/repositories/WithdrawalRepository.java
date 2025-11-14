package com.mabsplace.mabsplaceback.domain.repositories;

import com.mabsplace.mabsplaceback.domain.entities.Withdrawal;
import com.mabsplace.mabsplaceback.domain.enums.WithdrawalOperator;
import com.mabsplace.mabsplaceback.domain.enums.WithdrawalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WithdrawalRepository extends JpaRepository<Withdrawal, Long> {

  Optional<Withdrawal> findByAppTransactionRef(String appTransactionRef);

  Optional<Withdrawal> findByCoolpayTransactionRef(String coolpayTransactionRef);

  List<Withdrawal> findByCreatedById(Long userId);

  List<Withdrawal> findByStatus(WithdrawalStatus status);

  List<Withdrawal> findByTransactionOperator(WithdrawalOperator operator);

  List<Withdrawal> findByStatusOrderByCreatedAtDesc(WithdrawalStatus status);

  List<Withdrawal> findAllByOrderByCreatedAtDesc();
}
