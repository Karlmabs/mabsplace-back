package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.digitalgoods.ServiceFeeConfigDto;
import com.mabsplace.mabsplaceback.domain.entities.ProductCategory;
import com.mabsplace.mabsplaceback.domain.entities.ServiceFeeConfig;
import com.mabsplace.mabsplaceback.domain.mappers.ServiceFeeConfigMapper;
import com.mabsplace.mabsplaceback.domain.repositories.ProductCategoryRepository;
import com.mabsplace.mabsplaceback.domain.repositories.ServiceFeeConfigRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ServiceFeeService {

    private final ServiceFeeConfigRepository serviceFeeConfigRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ServiceFeeConfigMapper serviceFeeConfigMapper;
    private static final Logger logger = LoggerFactory.getLogger(ServiceFeeService.class);

    public ServiceFeeService(ServiceFeeConfigRepository serviceFeeConfigRepository,
                              ProductCategoryRepository productCategoryRepository,
                              ServiceFeeConfigMapper serviceFeeConfigMapper) {
        this.serviceFeeConfigRepository = serviceFeeConfigRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.serviceFeeConfigMapper = serviceFeeConfigMapper;
    }

    public ServiceFeeConfigDto createServiceFee(ServiceFeeConfigDto serviceFeeDto) {
        logger.info("Creating service fee config: {}", serviceFeeDto.getName());

        ServiceFeeConfig serviceFee = serviceFeeConfigMapper.toEntity(serviceFeeDto);

        if (serviceFeeDto.getProductCategoryId() != null) {
            ProductCategory category = productCategoryRepository.findById(serviceFeeDto.getProductCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", "id",
                            serviceFeeDto.getProductCategoryId()));
            serviceFee.setProductCategory(category);
        }

        ServiceFeeConfig saved = serviceFeeConfigRepository.save(serviceFee);
        logger.info("Service fee config created with ID: {}", saved.getId());
        return serviceFeeConfigMapper.toDto(saved);
    }

    public ServiceFeeConfigDto updateServiceFee(Long id, ServiceFeeConfigDto serviceFeeDto) {
        logger.info("Updating service fee config ID: {}", id);
        ServiceFeeConfig serviceFee = serviceFeeConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceFeeConfig", "id", id));

        if (serviceFeeDto.getProductCategoryId() != null) {
            ProductCategory category = productCategoryRepository.findById(serviceFeeDto.getProductCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", "id",
                            serviceFeeDto.getProductCategoryId()));
            serviceFee.setProductCategory(category);
        }

        serviceFeeConfigMapper.partialUpdate(serviceFeeDto, serviceFee);
        ServiceFeeConfig updated = serviceFeeConfigRepository.save(serviceFee);
        logger.info("Service fee config updated: {}", updated.getId());
        return serviceFeeConfigMapper.toDto(updated);
    }

    public void deleteServiceFee(Long id) {
        logger.info("Deleting service fee config ID: {}", id);
        serviceFeeConfigRepository.deleteById(id);
        logger.info("Service fee config deleted: {}", id);
    }

    public ServiceFeeConfigDto getServiceFeeById(Long id) {
        logger.info("Fetching service fee config ID: {}", id);
        ServiceFeeConfig serviceFee = serviceFeeConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceFeeConfig", "id", id));
        return serviceFeeConfigMapper.toDto(serviceFee);
    }

    public List<ServiceFeeConfigDto> getAllServiceFees() {
        logger.info("Fetching all service fee configs");
        List<ServiceFeeConfig> serviceFees = serviceFeeConfigRepository.findAll();
        logger.info("Found {} service fee configs", serviceFees.size());
        return serviceFeeConfigMapper.toDtoList(serviceFees);
    }
}
