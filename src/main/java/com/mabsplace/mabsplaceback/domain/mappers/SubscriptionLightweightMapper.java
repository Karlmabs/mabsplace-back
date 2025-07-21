package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.subscription.SubscriptionLightweightResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Subscription;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface SubscriptionLightweightMapper {

    @Mapping(target = "userId", expression = "java(getUserId(subscription))")
    @Mapping(target = "username", expression = "java(getUsername(subscription))")
    @Mapping(target = "serviceName", expression = "java(getServiceName(subscription))")
    @Mapping(target = "subscriptionPlanName", expression = "java(getSubscriptionPlanName(subscription))")
    SubscriptionLightweightResponseDto toDto(Subscription subscription);

    default Long getUserId(Subscription subscription) {
        if (subscription == null || subscription.getUser() == null) {
            return null;
        }
        return subscription.getUser().getId();
    }

    default String getUsername(Subscription subscription) {
        if (subscription == null || subscription.getUser() == null) {
            return null;
        }
        return subscription.getUser().getUsername();
    }

    default String getServiceName(Subscription subscription) {
        if (subscription == null || subscription.getService() == null) {
            return null;
        }
        return subscription.getService().getName();
    }

    default String getSubscriptionPlanName(Subscription subscription) {
        if (subscription == null || subscription.getSubscriptionPlan() == null) {
            return null;
        }
        return subscription.getSubscriptionPlan().getName();
    }

    List<SubscriptionLightweightResponseDto> toDtoList(List<Subscription> subscriptions);
}
