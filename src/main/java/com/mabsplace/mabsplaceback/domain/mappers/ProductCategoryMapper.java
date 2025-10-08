package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.digitalgoods.ProductCategoryDto;
import com.mabsplace.mabsplaceback.domain.entities.ProductCategory;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductCategoryMapper {
    ProductCategory toEntity(ProductCategoryDto productCategoryDto);

    ProductCategoryDto toDto(ProductCategory productCategory);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ProductCategory partialUpdate(ProductCategoryDto productCategoryDto, @MappingTarget ProductCategory productCategory);

    List<ProductCategoryDto> toDtoList(List<ProductCategory> productCategories);
}
