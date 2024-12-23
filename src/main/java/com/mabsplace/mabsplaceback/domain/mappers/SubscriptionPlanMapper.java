package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.subscriptionPlan.SubscriptionPlanRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.subscriptionPlan.SubscriptionPlanResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.MyService;
import com.mabsplace.mabsplaceback.domain.entities.SubscriptionPlan;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface SubscriptionPlanMapper {

  SubscriptionPlan toEntity(SubscriptionPlanRequestDto subscriptionPlanRequestDto);

  @Mapping(target = "myServiceId", expression = "java(mapService(subscriptionPlan.getMyService()))")
  @Mapping(target = "serviceName", expression = "java(subscriptionPlan.getMyService().getName())")
  @Mapping(target = "originalPrice", source = "price")
  @Mapping(target = "finalPrice", expression = "java(calculateFinalPrice(subscriptionPlan))")
  @Mapping(target = "discountPercentage", expression = "java(getActiveDiscountPercentage(subscriptionPlan))")
  @Mapping(target = "hasActiveDiscount", expression = "java(hasActiveDiscount(subscriptionPlan))")
  SubscriptionPlanResponseDto toDto(SubscriptionPlan subscriptionPlan);

  default Long mapService(MyService myService) {
    if (myService == null) {
      return 0L;
    }
    return myService.getId();
  }

  @Named("calculateFinalPrice")
  default BigDecimal calculateFinalPrice(SubscriptionPlan plan) {
    // This will be implemented in the mapper implementation
    return plan.getPrice();
  }

  @Named("getActiveDiscountPercentage")
  default BigDecimal getActiveDiscountPercentage(SubscriptionPlan plan) {
    // This will be implemented in the mapper implementation
    return BigDecimal.ZERO;
  }

  @Named("hasActiveDiscount")
  default boolean hasActiveDiscount(SubscriptionPlan plan) {
    // This will be implemented in the mapper implementation
    return false;
  }

  List<SubscriptionPlanResponseDto> toDtoList(List<SubscriptionPlan> subscriptionPlans);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  SubscriptionPlan partialUpdate(SubscriptionPlanRequestDto subscriptionPlanRequestDto, @MappingTarget SubscriptionPlan subscriptionPlan);

}
