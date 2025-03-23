package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.servicePackage.ServicePackageRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.servicePackage.ServicePackageResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.MyService;
import com.mabsplace.mabsplaceback.domain.entities.ServicePackage;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {MyServiceMapper.class})
public interface ServicePackageMapper {

    @Mapping(target = "services", ignore = true)
    ServicePackage toEntity(ServicePackageRequestDto dto);

    ServicePackageResponseDto toDto(ServicePackage entity);

    List<ServicePackageResponseDto> toDtoList(List<ServicePackage> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "services", ignore = true)
    ServicePackage partialUpdate(ServicePackageRequestDto dto, @MappingTarget ServicePackage entity);
}
