package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.expenseCategory.ExpenseCategoryRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.expenseCategory.ExpenseCategoryResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.ExpenseCategory;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ExpenseCategoryMapper {

    ExpenseCategoryResponseDto toResponseDTO(ExpenseCategory expenseCategory);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ExpenseCategory toEntity(ExpenseCategoryRequestDto requestDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(ExpenseCategoryRequestDto requestDTO, @MappingTarget ExpenseCategory expenseCategory);
}