package com.mabsplace.mabsplaceback.domain.repositories;

import com.mabsplace.mabsplaceback.domain.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUserId(Long userId);

    int countByUserId(Long userId);

    // New method to count payments by user ID where subscription plan name is not "Trial"
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.user.id = :userId AND p.subscriptionPlan.name <> :planName")
    int countByUserIdAndSubscriptionPlanNameNot(@Param("userId") Long userId, @Param("planName") String planName);
}
