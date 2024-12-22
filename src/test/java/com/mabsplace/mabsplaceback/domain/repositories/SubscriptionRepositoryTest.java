package com.mabsplace.mabsplaceback.domain.repositories;

import com.mabsplace.mabsplaceback.domain.entities.Subscription;
import com.mabsplace.mabsplaceback.domain.enums.SubscriptionStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class SubscriptionRepositoryTest {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Test
    public void testFindByStatusAndEndDateBeforeAndAutoRenewTrue() {
        // Given
        Subscription subscription = new Subscription();
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setEndDate(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24)); // yesterday
        subscription.setAutoRenew(true);
        subscriptionRepository.save(subscription);

        // When
        List<Subscription> result = subscriptionRepository.findByStatusAndEndDateBeforeAndAutoRenewTrue(SubscriptionStatus.ACTIVE, new Date());

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(result.get(0).isAutoRenew()).isTrue();
    }
}