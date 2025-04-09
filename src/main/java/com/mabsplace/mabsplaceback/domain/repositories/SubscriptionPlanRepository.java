package com.mabsplace.mabsplaceback.domain.repositories;

import com.mabsplace.mabsplaceback.domain.entities.SubscriptionPlan;
import com.mabsplace.mabsplaceback.domain.enums.Period;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    List<SubscriptionPlan> findByMyServiceId(Long myServiceId);

    boolean existsByMyServiceIdAndPeriodAndIdNot(Long myServiceId, Period period, Long id);
    boolean existsByMyServiceIdAndNameIgnoreCaseAndIdNot(Long myServiceId, String name, Long id);

    boolean existsByMyServiceIdAndPeriod(Long myServiceId, Period period);
    boolean existsByMyServiceIdAndNameIgnoreCase(Long myServiceId, String name);
}
