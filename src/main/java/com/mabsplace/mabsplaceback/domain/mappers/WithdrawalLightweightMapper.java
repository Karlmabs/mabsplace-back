package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.withdrawal.WithdrawalLightweightResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Withdrawal;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface WithdrawalLightweightMapper {

  @Mapping(target = "currencySymbol", expression = "java(getCurrencySymbol(withdrawal))")
  @Mapping(target = "currencyName", expression = "java(getCurrencyName(withdrawal))")
  @Mapping(target = "createdByUsername", expression = "java(getCreatedByUsername(withdrawal))")
  WithdrawalLightweightResponseDto toDto(Withdrawal withdrawal);

  default String getCurrencySymbol(Withdrawal withdrawal) {
    if (withdrawal == null || withdrawal.getCurrency() == null) {
      return null;
    }
    return withdrawal.getCurrency().getSymbol();
  }

  default String getCurrencyName(Withdrawal withdrawal) {
    if (withdrawal == null || withdrawal.getCurrency() == null) {
      return null;
    }
    return withdrawal.getCurrency().getName();
  }

  default String getCreatedByUsername(Withdrawal withdrawal) {
    if (withdrawal == null || withdrawal.getCreatedBy() == null) {
      return null;
    }
    return withdrawal.getCreatedBy().getUsername();
  }

  List<WithdrawalLightweightResponseDto> toDtoList(List<Withdrawal> withdrawals);
}
