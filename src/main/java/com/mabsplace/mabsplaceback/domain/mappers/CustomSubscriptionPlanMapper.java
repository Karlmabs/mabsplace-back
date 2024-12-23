package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.subscriptionPlan.SubscriptionPlanRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.subscriptionPlan.SubscriptionPlanResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.MyService;
import com.mabsplace.mabsplaceback.domain.entities.SubscriptionPlan;
import com.mabsplace.mabsplaceback.domain.services.SubscriptionDiscountService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Primary
@Component("customSubscriptionPlanMapper")
public class CustomSubscriptionPlanMapper implements SubscriptionPlanMapper {
    private final SubscriptionPlanMapper delegate;
    private final SubscriptionDiscountService discountService;

    public CustomSubscriptionPlanMapper(
            @Qualifier("subscriptionPlanMapperImpl") SubscriptionPlanMapper delegate,
            SubscriptionDiscountService discountService) {
        this.delegate = delegate;
        this.discountService = discountService;
    }

    @Override
    public SubscriptionPlan toEntity(SubscriptionPlanRequestDto dto) {
        return delegate.toEntity(dto);
    }

    @Override
    public SubscriptionPlanResponseDto toDto(SubscriptionPlan plan) {
        SubscriptionPlanResponseDto dto = delegate.toDto(plan);
        dto.setFinalPrice(calculateFinalPrice(plan));
        dto.setDiscountPercentage(getActiveDiscountPercentage(plan));
        dto.setHasActiveDiscount(hasActiveDiscount(plan));
        return dto;
    }

    @Override
    public Long mapService(MyService myService) {
        return delegate.mapService(myService);
    }

    @Override
    public List<SubscriptionPlanResponseDto> toDtoList(List<SubscriptionPlan> subscriptionPlans) {
        return subscriptionPlans.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public SubscriptionPlan partialUpdate(SubscriptionPlanRequestDto dto, SubscriptionPlan entity) {
        return delegate.partialUpdate(dto, entity);
    }

    @Override
    public BigDecimal calculateFinalPrice(SubscriptionPlan plan) {
        return discountService.getDiscountedPrice(plan);
    }

    @Override
    public BigDecimal getActiveDiscountPercentage(SubscriptionPlan plan) {
        return discountService.getActiveDiscountPercentage(plan);
    }

    @Override
    public boolean hasActiveDiscount(SubscriptionPlan plan) {
        return discountService.hasActiveDiscount(plan);
    }
}