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
  @Mapping(target = "accountId", expression = "java(mapAccount(subscription.getProfile()))")
  @Mapping(target = "serviceId", expression = "java(subscription.getService().getId())")
  @Mapping(target = "serviceName", expression = "java(mapServiceName(subscription.getService().getName()))")
  @Mapping(target = "serviceLogo", expression = "java(mapServiceName(subscription.getService().getLogo()))")
  @Mapping(target = "username", expression = "java(subscription.getUser().getUsername())")
  @Mapping(target = "profileName", expression = "java(mapProfileName(subscription.getProfile()))")
  @Mapping(target = "subscriptionPlanName", expression = "java(subscription.getSubscriptionPlan().getName())")
  @Mapping(target = "login", expression = "java(mapAccountLogin(subscription.getProfile()))")
  @Mapping(target = "password", expression = "java(mapAccountPassword(subscription.getProfile()))")
  @Mapping(target = "profilePin", expression = "java(mapProfilePin(subscription.getProfile()))")
  SubscriptionResponseDto toDto(Subscription subscription);

  default Long mapUser(User user) {
    if (user == null) {
      return 0L;
    }
    return user.getId();
  }

  default String mapServiceName(String serviceName) {
      return serviceName;
  }

  default String mapProfileName(Profile profile) {
    if (profile == null) {
      return "";
    }
    return profile.getProfileName();
  }

  default String mapAccountLogin(Profile profile) {
    if (profile == null) {
      return "";
    }
    return profile.getServiceAccount().getLogin();
  }

  default String mapAccountPassword(Profile profile) {
    if (profile == null) {
      return "";
    }
    return profile.getServiceAccount().getPassword();
  }

  default String mapProfilePin(Profile profile) {
    if (profile == null) {
      return "";
    }
    return profile.getPin();
  }

  default Long mapSubscriptionPlan(SubscriptionPlan subscriptionPlan) {
    if (subscriptionPlan == null) {
      return 0L;
    }
    return subscriptionPlan.getId();
  }

  default Long mapProfile(Profile profile) {
    if (profile == null) {
      return 0L;
    }
    return profile.getId();
  }

  default Long mapAccount(Profile profile) {
    if (profile == null) {
      return 0L;
    }
    return profile.getServiceAccount().getId();
  }

  List<SubscriptionResponseDto> toDtoList(List<Subscription> subscriptions);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  Subscription partialUpdate(SubscriptionRequestDto subscriptionRequestDto, @MappingTarget Subscription subscription);

}
