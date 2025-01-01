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
        LocalDateTime now = LocalDateTime.now();

        // Check for service-specific discounts
        List<ServiceDiscount> serviceDiscounts = discountRepository
                .findByServiceIdAndEndDateAfterAndStartDateBefore(
                        plan.getMyService().getId(), now, now);

        // Check for global discounts
        List<ServiceDiscount> globalDiscounts = discountRepository
                .findByIsGlobalTrueAndEndDateAfterAndStartDateBefore(now, now);

        // Combine and get the highest discount
        BigDecimal highestDiscount = Stream.concat(
                        serviceDiscounts.stream(),
                        globalDiscounts.stream())
                .map(ServiceDiscount::getDiscountPercentage)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        if (highestDiscount.equals(BigDecimal.ZERO)) {
            return plan.getPrice();
        }

        // Calculate discounted price
        BigDecimal discountMultiplier = BigDecimal.ONE.subtract(
                highestDiscount.divide(BigDecimal.valueOf(100)));
        return plan.getPrice().multiply(discountMultiplier)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public ServiceDiscountDTO createDiscount(ServiceDiscountDTO discountDTO) {
        ServiceDiscount discount = new ServiceDiscount();
        discount.setDiscountPercentage(discountDTO.getDiscountPercentage());
        discount.setStartDate(discountDTO.getStartDate());
        discount.setEndDate(discountDTO.getEndDate());
        discount.setService(discountDTO.getServiceId() != null ?
                serviceRepository.findById(discountDTO.getServiceId()).orElse(null) : null);
        log.debug("Setting isGlobal to: {}", discountDTO.isGlobal());
        System.out.println("Setting isGlobal to: " + discountDTO.isGlobal());
        discount.setGlobal(discountDTO.isGlobal());

        ServiceDiscount savedDiscount = discountRepository.save(discount);
        return discountMapper.toDTO(savedDiscount);
    }

    public ServiceDiscountDTO updateDiscount(ServiceDiscountDTO discountDTO) {
        ServiceDiscount discount = discountRepository.findById(discountDTO.getId()).orElse(null);
        if (discount == null) {
            return null;
        }

        discount.setDiscountPercentage(discountDTO.getDiscountPercentage());
        discount.setStartDate(discountDTO.getStartDate());
        discount.setEndDate(discountDTO.getEndDate());
        discount.setService(discountDTO.getServiceId() != null ?
                serviceRepository.findById(discountDTO.getServiceId()).orElse(null) : null);
        log.debug("Setting isGlobal to: {}", discountDTO.isGlobal());
        System.out.println("Setting isGlobal to: " + discountDTO.isGlobal());
        discount.setGlobal(discountDTO.isGlobal());

        ServiceDiscount savedDiscount = discountRepository.save(discount);
        return discountMapper.toDTO(savedDiscount);
    }

    public void deleteDiscount(Long id) {
        discountRepository.deleteById(id);
    }

    public List<ServiceDiscountDTO> getAllDiscounts() {
        return discountMapper.toDTOs(discountRepository.findAll());
    }

    public ServiceDiscountDTO getDiscountById(Long id) {
        return discountMapper.toDTO(discountRepository.findById(id).orElse(null));
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

        // Check for service-specific discounts
        List<ServiceDiscount> serviceDiscounts = discountRepository
                .findByServiceIdAndEndDateAfterAndStartDateBefore(
                        plan.getMyService().getId(), now, now);

        // Check for global discounts
        List<ServiceDiscount> globalDiscounts = discountRepository
                .findByIsGlobalTrueAndEndDateAfterAndStartDateBefore(now, now);

        // Get the highest discount percentage
        return Stream.concat(serviceDiscounts.stream(), globalDiscounts.stream())
                .map(ServiceDiscount::getDiscountPercentage)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    public boolean hasActiveDiscount(SubscriptionPlan plan) {
        return getActiveDiscountPercentage(plan).compareTo(BigDecimal.ZERO) > 0;
    }
}
