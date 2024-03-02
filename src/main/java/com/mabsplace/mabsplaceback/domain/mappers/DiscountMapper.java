package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.discount.DiscountRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.discount.DiscountResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Discount;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface DiscountMapper {

    Discount toEntity(DiscountRequestDto discountRequestDto);

    @Mapping(target = "userId", expression = "java(discount.getUser().getId())")
    DiscountResponseDto toDto(Discount discount);

    List<DiscountResponseDto> toDtoList(List<Discount> discounts);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Discount partialUpdate(DiscountRequestDto discountRequestDto, @MappingTarget Discount discount);
    
}
