package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.transaction.TransactionRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.transaction.TransactionResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Transaction;
import com.mabsplace.mabsplaceback.domain.entities.Wallet;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {CurrencyMapper.class, WalletMapper.class})
public interface TransactionMapper {

  Transaction toEntity(TransactionRequestDto transactionRequestDto);

  @Mapping(target = "receiverWalletId" , expression = "java(mapReceiverWallet(transaction.getReceiverWallet()))")
  @Mapping(target = "senderWalletId" , expression = "java(mapSenderWallet(transaction.getSenderWallet()))")
  TransactionResponseDto toDto(Transaction transaction);

  default Long mapReceiverWallet(Wallet wallet) {
    if (wallet == null) {
      return 0L;
    }
    return wallet.getId();
  }

  default Long mapSenderWallet(Wallet wallet) {
    if (wallet == null) {
      return 0L;
    }
    return wallet.getId();
  }

  List<TransactionResponseDto> toDtoList(List<Transaction> transactions);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  Transaction partialUpdate(TransactionRequestDto transactionRequestDto, @MappingTarget Transaction transaction);

}
