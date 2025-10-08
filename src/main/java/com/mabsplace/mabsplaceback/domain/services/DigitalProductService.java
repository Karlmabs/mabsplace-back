package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.digitalgoods.DigitalProductDto;
import com.mabsplace.mabsplaceback.domain.entities.DigitalProduct;
import com.mabsplace.mabsplaceback.domain.entities.ProductCategory;
import com.mabsplace.mabsplaceback.domain.mappers.DigitalProductMapper;
import com.mabsplace.mabsplaceback.domain.repositories.DigitalProductRepository;
import com.mabsplace.mabsplaceback.domain.repositories.ProductCategoryRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DigitalProductService {

    private final DigitalProductRepository digitalProductRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final DigitalProductMapper digitalProductMapper;
    private static final Logger logger = LoggerFactory.getLogger(DigitalProductService.class);

    public DigitalProductService(DigitalProductRepository digitalProductRepository,
                                  ProductCategoryRepository productCategoryRepository,
                                  DigitalProductMapper digitalProductMapper) {
        this.digitalProductRepository = digitalProductRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.digitalProductMapper = digitalProductMapper;
    }

    public DigitalProductDto createProduct(DigitalProductDto productDto) {
        logger.info("Creating digital product: {}", productDto.getName());

        ProductCategory category = productCategoryRepository.findById(productDto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", "id", productDto.getCategoryId()));

        DigitalProduct product = digitalProductMapper.toEntity(productDto);
        product.setCategory(category);

        DigitalProduct saved = digitalProductRepository.save(product);
        logger.info("Digital product created with ID: {}", saved.getId());
        return digitalProductMapper.toDto(saved);
    }

    public DigitalProductDto updateProduct(Long id, DigitalProductDto productDto) {
        logger.info("Updating digital product ID: {}", id);
        DigitalProduct product = digitalProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DigitalProduct", "id", id));

        if (productDto.getCategoryId() != null) {
            ProductCategory category = productCategoryRepository.findById(productDto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", "id", productDto.getCategoryId()));
            product.setCategory(category);
        }

        digitalProductMapper.partialUpdate(productDto, product);
        DigitalProduct updated = digitalProductRepository.save(product);
        logger.info("Digital product updated: {}", updated.getId());
        return digitalProductMapper.toDto(updated);
    }

    public void deleteProduct(Long id) {
        logger.info("Deleting digital product ID: {}", id);
        digitalProductRepository.deleteById(id);
        logger.info("Digital product deleted: {}", id);
    }

    public DigitalProductDto getProductById(Long id) {
        logger.info("Fetching digital product ID: {}", id);
        DigitalProduct product = digitalProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DigitalProduct", "id", id));
        return digitalProductMapper.toDto(product);
    }

    public List<DigitalProductDto> getAllProducts() {
        logger.info("Fetching all digital products");
        List<DigitalProduct> products = digitalProductRepository.findAll();
        logger.info("Found {} products", products.size());
        return digitalProductMapper.toDtoList(products);
    }

    public List<DigitalProductDto> getActiveProducts() {
        logger.info("Fetching active digital products");
        List<DigitalProduct> products = digitalProductRepository.findByIsActiveTrue();
        logger.info("Found {} active products", products.size());
        return digitalProductMapper.toDtoList(products);
    }

    public List<DigitalProductDto> getProductsByCategory(Long categoryId) {
        logger.info("Fetching digital products for category ID: {}", categoryId);
        List<DigitalProduct> products = digitalProductRepository.findByCategoryId(categoryId);
        logger.info("Found {} products", products.size());
        return digitalProductMapper.toDtoList(products);
    }
}
