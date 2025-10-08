package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.digitalgoods.DigitalProductDto;
import com.mabsplace.mabsplaceback.domain.entities.DigitalProduct;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface DigitalProductMapper {
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    DigitalProductDto toDto(DigitalProduct digitalProduct);

    @Mapping(source = "categoryId", target = "category.id")
    DigitalProduct toEntity(DigitalProductDto digitalProductDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    DigitalProduct partialUpdate(DigitalProductDto digitalProductDto, @MappingTarget DigitalProduct digitalProduct);

    List<DigitalProductDto> toDtoList(List<DigitalProduct> digitalProducts);
}
