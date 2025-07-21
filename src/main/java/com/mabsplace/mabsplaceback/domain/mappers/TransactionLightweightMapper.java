package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.transaction.TransactionLightweightResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Transaction;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionLightweightMapper {

    @Mapping(target = "currencySymbol", expression = "java(getCurrencySymbol(transaction))")
    @Mapping(target = "transactionType", expression = "java(getTransactionType(transaction))")
    TransactionLightweightResponseDto toDto(Transaction transaction);

    default String getCurrencySymbol(Transaction transaction) {
        if (transaction == null || transaction.getCurrency() == null) {
            return null;
        }
        return transaction.getCurrency().getSymbol();
    }

    default String getTransactionType(Transaction transaction) {
        if (transaction == null || transaction.getTransactionType() == null) {
            return null;
        }
        return transaction.getTransactionType().toString();
    }

    List<TransactionLightweightResponseDto> toDtoList(List<Transaction> transactions);
}
