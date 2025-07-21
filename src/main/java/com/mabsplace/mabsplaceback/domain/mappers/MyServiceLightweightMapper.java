package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.myService.MyServiceLightweightResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.MyService;
import com.mabsplace.mabsplaceback.domain.entities.SubscriptionPlan;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface MyServiceLightweightMapper {

    @Mapping(target = "subscriptionPlanCount", expression = "java(getSubscriptionPlanCount(myService))")
    @Mapping(target = "serviceAccountCount", expression = "java(getServiceAccountCount(myService))")
    @Mapping(target = "planNames", expression = "java(getPlanNames(myService))")
    MyServiceLightweightResponseDto toDto(MyService myService);

    default Integer getSubscriptionPlanCount(MyService myService) {
        if (myService == null || myService.getSubscriptionPlans() == null) {
            return 0;
        }
        return myService.getSubscriptionPlans().size();
    }

    default Integer getServiceAccountCount(MyService myService) {
        if (myService == null || myService.getServiceAccounts() == null) {
            return 0;
        }
        return myService.getServiceAccounts().size();
    }

    default List<String> getPlanNames(MyService myService) {
        if (myService == null || myService.getSubscriptionPlans() == null) {
            return List.of();
        }
        return myService.getSubscriptionPlans().stream()
                .map(SubscriptionPlan::getName)
                .collect(Collectors.toList());
    }

    List<MyServiceLightweightResponseDto> toDtoList(List<MyService> myServices);
}
