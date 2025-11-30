package com.mabsplace.mabsplaceback.domain.repositories;

import com.mabsplace.mabsplaceback.domain.entities.Subscription;
import com.mabsplace.mabsplaceback.domain.entities.User;
import com.mabsplace.mabsplaceback.domain.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByUserId(Long userId);

    List<Subscription> findByStatus(SubscriptionStatus status);

    List<Subscription> findByEndDateBeforeAndStatusNot(Date date, SubscriptionStatus subscriptionStatus);

    List<Subscription> findByStatusAndEndDateBeforeAndAutoRenewTrue(SubscriptionStatus subscriptionStatus, Date today);

    List<Subscription> findByProfileId(Long id);

    List<Subscription> findByEndDateBeforeAndStatusNotAndAutoRenewFalse(Date date, SubscriptionStatus subscriptionStatus);

    /**
     * Finds subscriptions that should be expired because either:
     * 1. autoRenew is false (user opted out), OR
     * 2. autoRenew is true BUT renewalAttempts >= 4 (exhausted all retries)
     *
     * This prevents premature expiration of subscriptions still in the renewal retry window.
     */
    @Query("SELECT s FROM Subscription s WHERE s.endDate < :date AND s.status <> :status " +
           "AND (s.autoRenew = false OR COALESCE(s.renewalAttempts, 0) >= 4)")
    List<Subscription> findSubscriptionsToExpire(@Param("date") Date date,
                                                  @Param("status") SubscriptionStatus status);

    boolean existsByUserIdAndServiceIdAndIsTrial(Long id, Long id1, boolean b);

    // Check if user has ever had a trial for this service (regardless of current status)
    boolean existsByUserIdAndServiceIdAndIsTrialTrue(Long userId, Long serviceId);

    boolean existsByUserIdAndServiceIdAndStatusAndEndDateAfter(Long id, Long id1, SubscriptionStatus subscriptionStatus, Date date);

    // Check if a profile is already in use by subscriptions with specific statuses
    boolean existsByProfileIdAndStatusIn(Long profileId, List<SubscriptionStatus> statuses);

    List<Subscription> findByEndDateBetweenAndStatusNotAndAutoRenewFalse(Date startDate, Date endDate, SubscriptionStatus subscriptionStatus);

    List<Subscription> findByEndDateBetweenAndStatusNotAndAutoRenewFalseAndExpirationNotifiedFalse(Date startDate, Date endDate, SubscriptionStatus subscriptionStatus);

    List<Subscription> findByEndDateBetweenAndStatusNotAndExpirationNotifiedFalse(Date startDate, Date endDate, SubscriptionStatus subscriptionStatus);

    @Query("SELECT DISTINCT s.user FROM Subscription s " +
           "WHERE s.endDate < :cutoffDate " +
           "AND NOT EXISTS (" +
           "  SELECT 1 FROM Subscription s2 " +
           "  WHERE s2.user.id = s.user.id " +
           "  AND s2.endDate >= :cutoffDate" +
           ") " +
           "ORDER BY s.endDate DESC")
    List<User> findInactiveCustomersSince(@Param("cutoffDate") Date cutoffDate);
}
