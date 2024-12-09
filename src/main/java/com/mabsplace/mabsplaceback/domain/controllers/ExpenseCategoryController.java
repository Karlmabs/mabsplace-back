package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.expenseCategory.ExpenseCategoryRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.expenseCategory.ExpenseCategoryResponseDto;
import com.mabsplace.mabsplaceback.domain.services.ExpenseCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expense-categories")
@RequiredArgsConstructor
@Tag(name = "Expense Categories", description = "Expense Categories management endpoints")
public class ExpenseCategoryController {

    private final ExpenseCategoryService expenseCategoryService;

    @GetMapping
    @Operation(summary = "Get all expense categories")
    @ApiResponse(responseCode = "200", description = "List of expense categories retrieved successfully")
    public ResponseEntity<List<ExpenseCategoryResponseDto>> getAllCategories() {
        return ResponseEntity.ok(expenseCategoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get expense category by ID")
    @ApiResponse(responseCode = "200", description = "Expense category retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Expense category not found")
    public ResponseEntity<ExpenseCategoryResponseDto> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseCategoryService.getCategoryById(id));
    }

    @PostMapping
    @Operation(summary = "Create new expense category")
    @ApiResponse(responseCode = "201", description = "Expense category created successfully")
    public ResponseEntity<ExpenseCategoryResponseDto> createCategory(
            @Valid @RequestBody ExpenseCategoryRequestDto requestDTO) {
        return new ResponseEntity<>(expenseCategoryService.createCategory(requestDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update expense category")
    @ApiResponse(responseCode = "200", description = "Expense category updated successfully")
    @ApiResponse(responseCode = "404", description = "Expense category not found")
    public ResponseEntity<ExpenseCategoryResponseDto> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseCategoryRequestDto requestDTO) {
        return ResponseEntity.ok(expenseCategoryService.updateCategory(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete expense category")
    @ApiResponse(responseCode = "204", description = "Expense category deleted successfully")
    @ApiResponse(responseCode = "404", description = "Expense category not found")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        expenseCategoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}