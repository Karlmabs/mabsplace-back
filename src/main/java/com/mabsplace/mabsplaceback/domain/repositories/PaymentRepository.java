package com.mabsplace.mabsplaceback.domain.repositories;

import com.mabsplace.mabsplaceback.domain.entities.Payment;
import com.mabsplace.mabsplaceback.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUserId(Long userId);

    int countByUserId(Long userId);

    // New method to count payments by user ID where subscription plan name is not "Trial"
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.user.id = :userId AND p.subscriptionPlan.name <> :planName")
    int countByUserIdAndSubscriptionPlanNameNot(@Param("userId") Long userId, @Param("planName") String planName);

    // Find users with no PAID payments in the last 90 days (inactive customers)
    @Query("SELECT DISTINCT u FROM User u " +
           "WHERE u.id IN (" +
           "  SELECT p.user.id FROM Payment p " +
           "  WHERE p.status = com.mabsplace.mabsplaceback.domain.enums.PaymentStatus.PAID " +
           "  GROUP BY p.user.id " +
           "  HAVING MAX(p.paymentDate) < :cutoffDate" +
           ") " +
           "AND NOT EXISTS (" +
           "  SELECT 1 FROM Payment p2 " +
           "  WHERE p2.user.id = u.id " +
           "  AND p2.status = com.mabsplace.mabsplaceback.domain.enums.PaymentStatus.PAID " +
           "  AND p2.paymentDate >= :cutoffDate" +
           ")")
    List<User> findUsersWithNoRecentPaidPayments(@Param("cutoffDate") Date cutoffDate);

    // Get most recent PAID payment for a user (returns only the first result)
    Optional<Payment> findFirstByUserIdAndStatusOrderByPaymentDateDesc(
        Long userId,
        com.mabsplace.mabsplaceback.domain.enums.PaymentStatus status
    );
}
