package com.mabsplace.mabsplaceback.domain.repositories;

import com.mabsplace.mabsplaceback.domain.entities.ServiceAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceAccountRepository extends JpaRepository<ServiceAccount, Long> {
    List<ServiceAccount> findByMyServiceId(Long myServiceId);
    
    @Query("SELECT sa FROM ServiceAccount sa " +
           "JOIN Subscription s ON s.service.id = sa.myService.id " +
           "WHERE s.user.id = :userId AND sa.myService.id = :serviceId")
    List<ServiceAccount> findByUserIdAndServiceId(@Param("userId") Long userId, @Param("serviceId") Long serviceId);
}
