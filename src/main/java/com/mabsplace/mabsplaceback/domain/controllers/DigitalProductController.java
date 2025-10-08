package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.digitalgoods.DigitalProductDto;
import com.mabsplace.mabsplaceback.domain.services.DigitalProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/digital-products")
public class DigitalProductController {

    private final DigitalProductService digitalProductService;
    private static final Logger logger = LoggerFactory.getLogger(DigitalProductController.class);

    public DigitalProductController(DigitalProductService digitalProductService) {
        this.digitalProductService = digitalProductService;
    }

    @PostMapping
    public ResponseEntity<DigitalProductDto> createProduct(@RequestBody DigitalProductDto productDto) {
        logger.info("Creating digital product: {}", productDto.getName());
        DigitalProductDto created = digitalProductService.createProduct(productDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DigitalProductDto> updateProduct(@PathVariable Long id, @RequestBody DigitalProductDto productDto) {
        logger.info("Updating digital product ID: {}", id);
        DigitalProductDto updated = digitalProductService.updateProduct(id, productDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        logger.info("Deleting digital product ID: {}", id);
        digitalProductService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DigitalProductDto> getProductById(@PathVariable Long id) {
        logger.info("Fetching digital product ID: {}", id);
        DigitalProductDto product = digitalProductService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping
    public ResponseEntity<List<DigitalProductDto>> getAllProducts() {
        logger.info("Fetching all digital products");
        List<DigitalProductDto> products = digitalProductService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/active")
    public ResponseEntity<List<DigitalProductDto>> getActiveProducts() {
        logger.info("Fetching active digital products");
        List<DigitalProductDto> products = digitalProductService.getActiveProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<DigitalProductDto>> getProductsByCategory(@PathVariable Long categoryId) {
        logger.info("Fetching digital products for category ID: {}", categoryId);
        List<DigitalProductDto> products = digitalProductService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }
}
