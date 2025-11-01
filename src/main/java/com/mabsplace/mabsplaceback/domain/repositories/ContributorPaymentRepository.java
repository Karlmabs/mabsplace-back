package com.mabsplace.mabsplaceback.domain.repositories;

import com.mabsplace.mabsplaceback.domain.entities.ContributorPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContributorPaymentRepository extends JpaRepository<ContributorPayment, Long> {
    List<ContributorPayment> findByPaymentPeriod(String paymentPeriod);
    List<ContributorPayment> findByUserId(Long userId);
    List<ContributorPayment> findByPaymentStatus(ContributorPayment.PaymentStatus status);
    Optional<ContributorPayment> findByConfigIdAndPaymentPeriod(Long configId, String paymentPeriod);
    boolean existsByConfigIdAndPaymentPeriod(Long configId, String paymentPeriod);
    List<ContributorPayment> findByPaymentStatusOrderByCreatedAtDesc(ContributorPayment.PaymentStatus status);
}
