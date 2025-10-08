package com.mabsplace.mabsplaceback.domain.repositories;

import com.mabsplace.mabsplaceback.domain.entities.ProductCategory;
import com.mabsplace.mabsplaceback.domain.entities.ServiceFeeConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceFeeConfigRepository extends JpaRepository<ServiceFeeConfig, Long> {
    Optional<ServiceFeeConfig> findByProductCategoryAndIsActiveTrue(ProductCategory productCategory);
}
