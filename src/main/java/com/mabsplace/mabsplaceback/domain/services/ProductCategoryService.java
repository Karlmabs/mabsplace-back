package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.digitalgoods.ProductCategoryDto;
import com.mabsplace.mabsplaceback.domain.entities.ProductCategory;
import com.mabsplace.mabsplaceback.domain.mappers.ProductCategoryMapper;
import com.mabsplace.mabsplaceback.domain.repositories.ProductCategoryRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductCategoryService {

    private final ProductCategoryRepository productCategoryRepository;
    private final ProductCategoryMapper productCategoryMapper;
    private static final Logger logger = LoggerFactory.getLogger(ProductCategoryService.class);

    public ProductCategoryService(ProductCategoryRepository productCategoryRepository,
                                   ProductCategoryMapper productCategoryMapper) {
        this.productCategoryRepository = productCategoryRepository;
        this.productCategoryMapper = productCategoryMapper;
    }

    public ProductCategoryDto createCategory(ProductCategoryDto categoryDto) {
        logger.info("Creating product category: {}", categoryDto.getName());
        ProductCategory category = productCategoryMapper.toEntity(categoryDto);
        ProductCategory saved = productCategoryRepository.save(category);
        logger.info("Product category created with ID: {}", saved.getId());
        return productCategoryMapper.toDto(saved);
    }

    public ProductCategoryDto updateCategory(Long id, ProductCategoryDto categoryDto) {
        logger.info("Updating product category ID: {}", id);
        ProductCategory category = productCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", "id", id));

        productCategoryMapper.partialUpdate(categoryDto, category);
        ProductCategory updated = productCategoryRepository.save(category);
        logger.info("Product category updated: {}", updated.getId());
        return productCategoryMapper.toDto(updated);
    }

    public void deleteCategory(Long id) {
        logger.info("Deleting product category ID: {}", id);
        productCategoryRepository.deleteById(id);
        logger.info("Product category deleted: {}", id);
    }

    public ProductCategoryDto getCategoryById(Long id) {
        logger.info("Fetching product category ID: {}", id);
        ProductCategory category = productCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", "id", id));
        return productCategoryMapper.toDto(category);
    }

    public List<ProductCategoryDto> getAllCategories() {
        logger.info("Fetching all product categories");
        List<ProductCategory> categories = productCategoryRepository.findAll();
        logger.info("Found {} categories", categories.size());
        return productCategoryMapper.toDtoList(categories);
    }

    public List<ProductCategoryDto> getActiveCategories() {
        logger.info("Fetching active product categories");
        List<ProductCategory> categories = productCategoryRepository.findByActiveTrue();
        logger.info("Found {} active categories", categories.size());
        return productCategoryMapper.toDtoList(categories);
    }
}
