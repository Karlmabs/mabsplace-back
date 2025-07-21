package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.expense.ExpenseLightweightResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Expense;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ExpenseLightweightMapper {

    @Mapping(target = "categoryName", expression = "java(getCategoryName(expense))")
    @Mapping(target = "currencySymbol", expression = "java(getCurrencySymbol(expense))")
    @Mapping(target = "createdById", expression = "java(getCreatedById(expense))")
    @Mapping(target = "createdByName", expression = "java(getCreatedByName(expense))")
    ExpenseLightweightResponseDto toDto(Expense expense);

    default String getCategoryName(Expense expense) {
        if (expense == null || expense.getCategory() == null) {
            return null;
        }
        return expense.getCategory().getName();
    }

    default String getCurrencySymbol(Expense expense) {
        if (expense == null || expense.getCurrency() == null) {
            return null;
        }
        return expense.getCurrency().getSymbol();
    }

    default Long getCreatedById(Expense expense) {
        if (expense == null || expense.getCreatedBy() == null) {
            return null;
        }
        return expense.getCreatedBy().getId();
    }

    default String getCreatedByName(Expense expense) {
        if (expense == null || expense.getCreatedBy() == null) {
            return null;
        }
        return expense.getCreatedBy().getUsername();
    }

    List<ExpenseLightweightResponseDto> toDtoList(List<Expense> expenses);
}
