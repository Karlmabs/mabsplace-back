package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.digitalgoods.ExchangeRateDto;
import com.mabsplace.mabsplaceback.domain.entities.ExchangeRate;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ExchangeRateMapper {
    ExchangeRate toEntity(ExchangeRateDto exchangeRateDto);

    ExchangeRateDto toDto(ExchangeRate exchangeRate);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ExchangeRate partialUpdate(ExchangeRateDto exchangeRateDto, @MappingTarget ExchangeRate exchangeRate);

    List<ExchangeRateDto> toDtoList(List<ExchangeRate> exchangeRates);
}
