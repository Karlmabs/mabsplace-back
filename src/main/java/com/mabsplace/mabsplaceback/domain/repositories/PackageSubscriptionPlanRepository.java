package com.mabsplace.mabsplaceback.domain.repositories;

import com.mabsplace.mabsplaceback.domain.entities.PackageSubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageSubscriptionPlanRepository extends JpaRepository<PackageSubscriptionPlan, Long> {
    
    /**
     * Find all active subscription plans for a package
     */
    List<PackageSubscriptionPlan> findByServicePackageIdAndActiveTrue(Long packageId);
}
