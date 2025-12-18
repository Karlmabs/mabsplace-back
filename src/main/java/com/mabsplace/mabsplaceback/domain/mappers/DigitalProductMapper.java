package com.mabsplace.mabsplaceback.domain.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mabsplace.mabsplaceback.domain.dtos.digitalgoods.CustomInputFieldDto;
import com.mabsplace.mabsplaceback.domain.dtos.digitalgoods.DigitalProductDto;
import com.mabsplace.mabsplaceback.domain.entities.DigitalProduct;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class DigitalProductMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    public abstract DigitalProductDto toDto(DigitalProduct digitalProduct);

    @Mapping(source = "categoryId", target = "category.id")
    public abstract DigitalProduct toEntity(DigitalProductDto digitalProductDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract DigitalProduct partialUpdate(DigitalProductDto digitalProductDto, @MappingTarget DigitalProduct digitalProduct);

    public abstract List<DigitalProductDto> toDtoList(List<DigitalProduct> digitalProducts);

    // Custom mapping methods for JSON conversion
    protected List<CustomInputFieldDto> mapCustomInputFields(String json) {
        if (json == null || json.isEmpty() || json.equals("[]")) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<CustomInputFieldDto>>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    protected String mapCustomInputFields(List<CustomInputFieldDto> fields) {
        if (fields == null || fields.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(fields);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}
