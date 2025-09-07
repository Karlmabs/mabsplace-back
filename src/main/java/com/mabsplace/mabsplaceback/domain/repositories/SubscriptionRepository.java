package com.mabsplace.mabsplaceback.domain.repositories;

import com.mabsplace.mabsplaceback.domain.entities.Subscription;
import com.mabsplace.mabsplaceback.domain.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
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

    boolean existsByUserIdAndServiceIdAndIsTrial(Long id, Long id1, boolean b);

    // Check if user has ever had a trial for this service (regardless of current status)
    boolean existsByUserIdAndServiceIdAndIsTrialTrue(Long userId, Long serviceId);

    boolean existsByUserIdAndServiceIdAndStatusAndEndDateAfter(Long id, Long id1, SubscriptionStatus subscriptionStatus, Date date);

    // Check if a profile is already in use by subscriptions with specific statuses
    boolean existsByProfileIdAndStatusIn(Long profileId, List<SubscriptionStatus> statuses);

    List<Subscription> findByEndDateBetweenAndStatusNotAndAutoRenewFalse(Date startDate, Date endDate, SubscriptionStatus subscriptionStatus);
}
