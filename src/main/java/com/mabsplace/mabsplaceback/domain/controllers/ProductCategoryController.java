package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.digitalgoods.ProductCategoryDto;
import com.mabsplace.mabsplaceback.domain.services.ProductCategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-categories")
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;
    private static final Logger logger = LoggerFactory.getLogger(ProductCategoryController.class);

    public ProductCategoryController(ProductCategoryService productCategoryService) {
        this.productCategoryService = productCategoryService;
    }

    @PostMapping
    public ResponseEntity<ProductCategoryDto> createCategory(@RequestBody ProductCategoryDto categoryDto) {
        logger.info("Creating product category: {}", categoryDto.getName());
        ProductCategoryDto created = productCategoryService.createCategory(categoryDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductCategoryDto> updateCategory(@PathVariable Long id, @RequestBody ProductCategoryDto categoryDto) {
        logger.info("Updating product category ID: {}", id);
        ProductCategoryDto updated = productCategoryService.updateCategory(id, categoryDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        logger.info("Deleting product category ID: {}", id);
        productCategoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductCategoryDto> getCategoryById(@PathVariable Long id) {
        logger.info("Fetching product category ID: {}", id);
        ProductCategoryDto category = productCategoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @GetMapping
    public ResponseEntity<List<ProductCategoryDto>> getAllCategories() {
        logger.info("Fetching all product categories");
        List<ProductCategoryDto> categories = productCategoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/active")
    public ResponseEntity<List<ProductCategoryDto>> getActiveCategories() {
        logger.info("Fetching active product categories");
        List<ProductCategoryDto> categories = productCategoryService.getActiveCategories();
        return ResponseEntity.ok(categories);
    }
}
