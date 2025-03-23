package com.mabsplace.mabsplaceback.domain.repositories;

import com.mabsplace.mabsplaceback.domain.entities.ServicePackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicePackageRepository extends JpaRepository<ServicePackage, Long> {
    
    /**
     * Find all active service packages
     */
    List<ServicePackage> findByActiveTrue();
    
    /**
     * Find active service packages that include a specific service
     */
    @Query("SELECT p FROM ServicePackage p JOIN p.services s WHERE s.id = :serviceId AND p.active = true")
    List<ServicePackage> findActivePackagesByServiceId(Long serviceId);
}
