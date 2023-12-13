package com.mabsplace.mabsplaceback.domain.repositories;

import com.mabsplace.mabsplaceback.domain.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
