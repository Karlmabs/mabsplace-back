package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.subscription.SubscriptionRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.subscription.SubscriptionResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Profile;
import com.mabsplace.mabsplaceback.domain.entities.Subscription;
import com.mabsplace.mabsplaceback.domain.entities.SubscriptionPlan;
import com.mabsplace.mabsplaceback.domain.entities.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {UserMapper.class, SubscriptionPlanMapper.class})
public interface SubscriptionMapper {

  Subscription toEntity(SubscriptionRequestDto subscriptionRequestDto);

  @Mapping(target = "userId", expression = "java(mapUser(subscription.getUser()))")
  @Mapping(target = "subscriptionPlanId", expression = "java(mapSubscriptionPlan(subscription.getSubscriptionPlan()))")
  @Mapping(target = "profileId", expression = "java(mapProfile(subscription.getProfile()))")
  SubscriptionResponseDto toDto(Subscription subscription);

  default Long mapUser(User user) {
    if (user == null) {
      return null;
    }
    return user.getId();
  }

  default Long mapSubscriptionPlan(SubscriptionPlan subscriptionPlan) {
    if (subscriptionPlan == null) {
      return null;
    }
    return subscriptionPlan.getId();
  }

  default Long mapProfile(Profile profile) {
    if (profile == null) {
      return null;
    }
    return profile.getId();
  }

  List<SubscriptionResponseDto> toDtoList(List<Subscription> subscriptions);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  Subscription partialUpdate(SubscriptionRequestDto subscriptionRequestDto, @MappingTarget Subscription subscription);

}
