package com.mabsplace.mabsplaceback.domain.repositories;

import com.mabsplace.mabsplaceback.domain.entities.ServiceDiscount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ServiceDiscountRepository extends JpaRepository<ServiceDiscount, Long> {
    List<ServiceDiscount> findByServiceIdAndEndDateAfterAndStartDateBefore(
            Long serviceId, LocalDateTime now, LocalDateTime now2);

    List<ServiceDiscount> findByIsGlobalTrueAndEndDateAfterAndStartDateBefore(
            LocalDateTime now, LocalDateTime now2);
}
