package com.mabsplace.mabsplaceback.domain.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mabsplace.mabsplaceback.domain.dtos.digitalgoods.DigitalGoodsOrderDto;
import com.mabsplace.mabsplaceback.domain.entities.DigitalGoodsOrder;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class DigitalGoodsOrderMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "deliveredBy.id", target = "deliveredBy")
    @Mapping(source = "deliveredBy.username", target = "deliveredByUsername")
    public abstract DigitalGoodsOrderDto toDto(DigitalGoodsOrder digitalGoodsOrder);

    @Mapping(source = "userId", target = "user.id")
    @Mapping(source = "productId", target = "product.id")
    @Mapping(target = "deliveredBy", ignore = true)
    public abstract DigitalGoodsOrder toEntity(DigitalGoodsOrderDto digitalGoodsOrderDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "deliveredBy", ignore = true)
    public abstract DigitalGoodsOrder partialUpdate(DigitalGoodsOrderDto digitalGoodsOrderDto, @MappingTarget DigitalGoodsOrder digitalGoodsOrder);

    public abstract List<DigitalGoodsOrderDto> toDtoList(List<DigitalGoodsOrder> digitalGoodsOrders);

    // Custom mapping methods for JSON conversion
    protected Map<String, String> mapCustomerInputData(String json) {
        if (json == null || json.isEmpty() || json.equals("{}")) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyMap();
        }
    }

    protected String mapCustomerInputData(Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
