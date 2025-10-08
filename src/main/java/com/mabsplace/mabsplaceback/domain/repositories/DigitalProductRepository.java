package com.mabsplace.mabsplaceback.domain.repositories;

import com.mabsplace.mabsplaceback.domain.entities.DigitalProduct;
import com.mabsplace.mabsplaceback.domain.entities.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DigitalProductRepository extends JpaRepository<DigitalProduct, Long> {
    List<DigitalProduct> findByIsActiveTrue();
    List<DigitalProduct> findByCategoryAndIsActiveTrue(ProductCategory category);
    List<DigitalProduct> findByCategoryId(Long categoryId);
}
