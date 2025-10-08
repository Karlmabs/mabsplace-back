package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.digitalgoods.DigitalGoodsOrderDto;
import com.mabsplace.mabsplaceback.domain.entities.DigitalGoodsOrder;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface DigitalGoodsOrderMapper {
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "deliveredBy.id", target = "deliveredBy")
    @Mapping(source = "deliveredBy.username", target = "deliveredByUsername")
    DigitalGoodsOrderDto toDto(DigitalGoodsOrder digitalGoodsOrder);

    @Mapping(source = "userId", target = "user.id")
    @Mapping(source = "productId", target = "product.id")
    @Mapping(target = "deliveredBy", ignore = true)
    DigitalGoodsOrder toEntity(DigitalGoodsOrderDto digitalGoodsOrderDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "deliveredBy", ignore = true)
    DigitalGoodsOrder partialUpdate(DigitalGoodsOrderDto digitalGoodsOrderDto, @MappingTarget DigitalGoodsOrder digitalGoodsOrder);

    List<DigitalGoodsOrderDto> toDtoList(List<DigitalGoodsOrder> digitalGoodsOrders);
}
