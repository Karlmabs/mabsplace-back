package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.withdrawal.WithdrawalRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.withdrawal.WithdrawalResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Withdrawal;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {CurrencyMapper.class, UserMapper.class})
public interface WithdrawalMapper {

  Withdrawal toEntity(WithdrawalRequestDto withdrawalRequestDto);

  WithdrawalResponseDto toDto(Withdrawal withdrawal);

  List<WithdrawalResponseDto> toDtoList(List<Withdrawal> withdrawals);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  Withdrawal partialUpdate(WithdrawalRequestDto withdrawalRequestDto, @MappingTarget Withdrawal withdrawal);
}
