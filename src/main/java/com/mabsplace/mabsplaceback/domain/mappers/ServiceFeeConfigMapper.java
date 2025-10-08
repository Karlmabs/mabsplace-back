package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.digitalgoods.ServiceFeeConfigDto;
import com.mabsplace.mabsplaceback.domain.entities.ServiceFeeConfig;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ServiceFeeConfigMapper {
    @Mapping(source = "productCategory.id", target = "productCategoryId")
    @Mapping(source = "productCategory.name", target = "productCategoryName")
    ServiceFeeConfigDto toDto(ServiceFeeConfig serviceFeeConfig);

    @Mapping(source = "productCategoryId", target = "productCategory.id")
    ServiceFeeConfig toEntity(ServiceFeeConfigDto serviceFeeConfigDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ServiceFeeConfig partialUpdate(ServiceFeeConfigDto serviceFeeConfigDto, @MappingTarget ServiceFeeConfig serviceFeeConfig);

    List<ServiceFeeConfigDto> toDtoList(List<ServiceFeeConfig> serviceFeeConfigs);
}
