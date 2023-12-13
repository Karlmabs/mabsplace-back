package com.mabsplace.mabsplaceback.domain.mappers;


import com.mabsplace.mabsplaceback.domain.dtos.currency.CurrencyRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.currency.CurrencyResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Currency;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {WalletMapper.class})
public interface CurrencyMapper {
  Currency toEntity(CurrencyRequestDto currencyRequestDto);

  CurrencyResponseDto toDto(Currency currency);

  List<CurrencyResponseDto> toDtoList(List<Currency> currencys);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  Currency partialUpdate(CurrencyRequestDto currencyRequestDto, @MappingTarget Currency currency);
}
