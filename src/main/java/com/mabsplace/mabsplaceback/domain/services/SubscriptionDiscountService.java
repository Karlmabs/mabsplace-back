package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.discount.ServiceDiscountDTO;
import com.mabsplace.mabsplaceback.domain.entities.ServiceDiscount;
import com.mabsplace.mabsplaceback.domain.entities.SubscriptionPlan;
import com.mabsplace.mabsplaceback.domain.mappers.ServiceDiscountMapper;
import com.mabsplace.mabsplaceback.domain.repositories.MyServiceRepository;
import com.mabsplace.mabsplaceback.domain.repositories.ServiceDiscountRepository;
import com.mabsplace.mabsplaceback.domain.repositories.SubscriptionPlanRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Service
@Transactional
@Slf4j
public class SubscriptionDiscountService {
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionDiscountService.class);
    private final ServiceDiscountRepository discountRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final MyServiceRepository serviceRepository;
    private final ServiceDiscountMapper discountMapper;

    @Autowired
    public SubscriptionDiscountService(ServiceDiscountRepository discountRepository,
                                       SubscriptionPlanRepository subscriptionPlanRepository, MyServiceRepository serviceRepository, ServiceDiscountMapper discountMapper) {
        this.discountRepository = discountRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.serviceRepository = serviceRepository;
        this.discountMapper = discountMapper;
    }

    public BigDecimal getDiscountedPrice(SubscriptionPlan plan) {
        logger.info("Calculating discounted price for SubscriptionPlan ID: {}", plan.getId());
        LocalDateTime now = LocalDateTime.now();

        List<ServiceDiscount> serviceDiscounts = discountRepository
                .findByServiceIdAndEndDateAfterAndStartDateBefore(plan.getMyService().getId(), now, now);

        List<ServiceDiscount> globalDiscounts = discountRepository
                .findByIsGlobalTrueAndEndDateAfterAndStartDateBefore(now, now);

        BigDecimal highestDiscount = Stream.concat(serviceDiscounts.stream(), globalDiscounts.stream())
                .map(ServiceDiscount::getDiscountPercentage)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        if (highestDiscount.equals(BigDecimal.ZERO)) {
            logger.info("No active discounts found. Returning original price: {}", plan.getPrice());
            return plan.getPrice();
        }

        BigDecimal discountMultiplier = BigDecimal.ONE.subtract(highestDiscount.divide(BigDecimal.valueOf(100)));
        BigDecimal discountedPrice = plan.getPrice().multiply(discountMultiplier).setScale(2, RoundingMode.HALF_UP);
        logger.info("Discount applied: {}%, discounted price: {}", highestDiscount, discountedPrice);

        return discountedPrice;
    }

    public ServiceDiscountDTO createDiscount(ServiceDiscountDTO discountDTO) {
        logger.info("Creating new service discount: {}", discountDTO);
        ServiceDiscount discount = new ServiceDiscount();
        discount.setDiscountPercentage(discountDTO.getDiscountPercentage());
        discount.setStartDate(discountDTO.getStartDate());
        discount.setEndDate(discountDTO.getEndDate());
        discount.setService(discountDTO.getServiceId() != null ?
                serviceRepository.findById(discountDTO.getServiceId()).orElse(null) : null);
        discount.setGlobal(discountDTO.isGlobal());

        ServiceDiscount savedDiscount = discountRepository.save(discount);
        logger.info("Service discount created successfully: {}", savedDiscount);
        return discountMapper.toDTO(savedDiscount);
    }

    public ServiceDiscountDTO updateDiscount(ServiceDiscountDTO discountDTO) {
        logger.info("Updating service discount with ID: {}", discountDTO.getId());
        ServiceDiscount discount = discountRepository.findById(discountDTO.getId()).orElse(null);
        if (discount == null) {
            logger.warn("Service discount not found for ID: {}", discountDTO.getId());
            return null;
        }

        discount.setDiscountPercentage(discountDTO.getDiscountPercentage());
        discount.setStartDate(discountDTO.getStartDate());
        discount.setEndDate(discountDTO.getEndDate());
        discount.setService(discountDTO.getServiceId() != null ?
                serviceRepository.findById(discountDTO.getServiceId()).orElse(null) : null);
        discount.setGlobal(discountDTO.isGlobal());

        ServiceDiscount savedDiscount = discountRepository.save(discount);
        logger.info("Service discount updated successfully: {}", savedDiscount);
        return discountMapper.toDTO(savedDiscount);
    }

    public void deleteDiscount(Long id) {
        logger.info("Deleting service discount with ID: {}", id);
        discountRepository.deleteById(id);
        logger.info("Deleted service discount successfully with ID: {}", id);
    }

    public List<ServiceDiscountDTO> getAllDiscounts() {
        logger.info("Fetching all service discounts");
        List<ServiceDiscountDTO> discounts = discountMapper.toDTOs(discountRepository.findAll());
        logger.info("Retrieved {} service discounts", discounts.size());
        return discounts;
    }

    public ServiceDiscountDTO getDiscountById(Long id) {
        logger.info("Fetching service discount with ID: {}", id);
        ServiceDiscount discount = discountRepository.findById(id).orElse(null);
        if (discount == null) {
            logger.warn("Service discount not found for ID: {}", id);
            return null;
        }
        logger.info("Retrieved service discount successfully: {}", discount);
        return discountMapper.toDTO(discount);
    }

    public List<ServiceDiscountDTO> getDiscountsByServiceId(Long serviceId) {
        return discountMapper.toDTOs(discountRepository.findByServiceIdAndEndDateAfterAndStartDateBefore(
                serviceId, LocalDateTime.now(), LocalDateTime.now()));
    }

    public List<ServiceDiscountDTO> getGlobalDiscounts() {
        return discountMapper.toDTOs(discountRepository.findByIsGlobalTrueAndEndDateAfterAndStartDateBefore(
                LocalDateTime.now(), LocalDateTime.now()));
    }

    public BigDecimal getActiveDiscountPercentage(SubscriptionPlan plan) {
        LocalDateTime now = LocalDateTime.now();

        List<ServiceDiscount> serviceDiscounts = discountRepository
                .findByServiceIdAndEndDateAfterAndStartDateBefore(
                        plan.getMyService().getId(), now, now);

        List<ServiceDiscount> globalDiscounts = discountRepository
                .findByIsGlobalTrueAndEndDateAfterAndStartDateBefore(now, now);

        return Stream.concat(serviceDiscounts.stream(), globalDiscounts.stream())
                .map(ServiceDiscount::getDiscountPercentage)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    public boolean hasActiveDiscount(SubscriptionPlan plan) {
        logger.info("Checking for active discounts for SubscriptionPlan ID: {}", plan.getId());
        boolean hasDiscount = getActiveDiscountPercentage(plan).compareTo(BigDecimal.ZERO) > 0;
        logger.info("Active discount found: {}", hasDiscount);
        return hasDiscount;
    }
}
