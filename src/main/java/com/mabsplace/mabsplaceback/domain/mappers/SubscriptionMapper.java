package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.subscription.SubscriptionRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.subscription.SubscriptionResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Subscription;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {UserMapper.class, ProfileMapper.class, SubscriptionPlanMapper.class})
public interface SubscriptionMapper {

  Subscription toEntity(SubscriptionRequestDto subscriptionRequestDto);

  SubscriptionResponseDto toDto(Subscription subscription);

  List<SubscriptionResponseDto> toDtoList(List<Subscription> subscriptions);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  Subscription partialUpdate(SubscriptionRequestDto subscriptionRequestDto, @MappingTarget Subscription subscription);

}
