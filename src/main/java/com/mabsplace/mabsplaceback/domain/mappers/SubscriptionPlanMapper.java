package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.subscriptionPlan.SubscriptionPlanRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.subscriptionPlan.SubscriptionPlanResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.SubscriptionPlan;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {MyServiceMapper.class})
public interface SubscriptionPlanMapper {

  SubscriptionPlan toEntity(SubscriptionPlanRequestDto subscriptionPlanRequestDto);

  SubscriptionPlanResponseDto toDto(SubscriptionPlan subscriptionPlan);

  List<SubscriptionPlanResponseDto> toDtoList(List<SubscriptionPlan> subscriptionPlans);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  SubscriptionPlan partialUpdate(SubscriptionPlanRequestDto subscriptionPlanRequestDto, @MappingTarget SubscriptionPlan subscriptionPlan);

}
