package com.mabsplace.mabsplaceback.domain.repositories;

import com.mabsplace.mabsplaceback.domain.entities.MyService;
import com.mabsplace.mabsplaceback.domain.entities.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface MyServiceRepository extends JpaRepository<MyService, Long> {

  @Query("SELECT s.subscriptionPlans FROM MyService s WHERE s.id = ?1")
  List<SubscriptionPlan> getSubscriptionPlansByServiceId(Long id);
}
