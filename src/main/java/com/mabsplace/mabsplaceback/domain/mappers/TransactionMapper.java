package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.transaction.TransactionRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.transaction.TransactionResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Transaction;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {CurrencyMapper.class, WalletMapper.class})
public interface TransactionMapper {

  Transaction toEntity(TransactionRequestDto transactionRequestDto);

  TransactionResponseDto toDto(Transaction transaction);

  List<TransactionResponseDto> toDtoList(List<Transaction> transactions);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  Transaction partialUpdate(TransactionRequestDto transactionRequestDto, @MappingTarget Transaction transaction);

}
