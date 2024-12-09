package com.mabsplace.mabsplaceback.domain.dtos.expenseCategory;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExpenseCategoryResponseDto {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
