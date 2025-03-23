package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.packageSubscriptionPlan.PackageSubscriptionPlanRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.packageSubscriptionPlan.PackageSubscriptionPlanResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.PackageSubscriptionPlan;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, 
        uses = {CurrencyMapper.class, ServicePackageMapper.class})
public interface PackageSubscriptionPlanMapper {

    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "servicePackage", ignore = true)
    PackageSubscriptionPlan toEntity(PackageSubscriptionPlanRequestDto dto);

    PackageSubscriptionPlanResponseDto toDto(PackageSubscriptionPlan entity);

    List<PackageSubscriptionPlanResponseDto> toDtoList(List<PackageSubscriptionPlan> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "servicePackage", ignore = true)
    PackageSubscriptionPlan partialUpdate(PackageSubscriptionPlanRequestDto dto, @MappingTarget PackageSubscriptionPlan entity);
}
