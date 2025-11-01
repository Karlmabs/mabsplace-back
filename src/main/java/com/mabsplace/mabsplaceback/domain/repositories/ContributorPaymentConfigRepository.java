package com.mabsplace.mabsplaceback.domain.repositories;

import com.mabsplace.mabsplaceback.domain.entities.ContributorPaymentConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContributorPaymentConfigRepository extends JpaRepository<ContributorPaymentConfig, Long> {
    List<ContributorPaymentConfig> findByIsActiveTrue();
    Optional<ContributorPaymentConfig> findByUserId(Long userId);
    List<ContributorPaymentConfig> findByUserIdIn(List<Long> userIds);
    boolean existsByUserId(Long userId);
}
