package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.subscriptionPlan.SubscriptionPlanRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.subscriptionPlan.SubscriptionPlanResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.MyService;
import com.mabsplace.mabsplaceback.domain.entities.SubscriptionPlan;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface SubscriptionPlanMapper {

  SubscriptionPlan toEntity(SubscriptionPlanRequestDto subscriptionPlanRequestDto);

  @Mapping(target = "myServiceId", expression = "java(mapService(subscriptionPlan.getMyService()))")
  @Mapping(target = "serviceName", expression = "java(subscriptionPlan.getMyService().getName())")
  SubscriptionPlanResponseDto toDto(SubscriptionPlan subscriptionPlan);

  default Long mapService(MyService myService) {
    if (myService == null) {
      return null;
    }
    return myService.getId();
  }

  List<SubscriptionPlanResponseDto> toDtoList(List<SubscriptionPlan> subscriptionPlans);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  SubscriptionPlan partialUpdate(SubscriptionPlanRequestDto subscriptionPlanRequestDto, @MappingTarget SubscriptionPlan subscriptionPlan);

}
