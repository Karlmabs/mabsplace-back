package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.expenseCategory.ExpenseCategoryRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.expenseCategory.ExpenseCategoryResponseDto;
import com.mabsplace.mabsplaceback.domain.services.ExpenseCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expense-categories")
@RequiredArgsConstructor
@Tag(name = "Expense Categories", description = "Expense Categories management endpoints")
public class ExpenseCategoryController {

    private final ExpenseCategoryService expenseCategoryService;
    private static final Logger logger = LoggerFactory.getLogger(ExpenseCategoryController.class);

    @PreAuthorize("@securityExpressionUtil.hasAnyRole(authentication, 'GET_EXPENSE_CATEGORIES')")
    @GetMapping
    @Operation(summary = "Get all expense categories")
    @ApiResponse(responseCode = "200", description = "List of expense categories retrieved successfully")
    public ResponseEntity<List<ExpenseCategoryResponseDto>> getAllCategories() {
        logger.info("Fetching all expense categories");
        List<ExpenseCategoryResponseDto> categories = expenseCategoryService.getAllCategories();
        logger.info("Fetched {} categories", categories.size());
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get expense category by ID")
    @ApiResponse(responseCode = "200", description = "Expense category retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Expense category not found")
    public ResponseEntity<ExpenseCategoryResponseDto> getCategoryById(@PathVariable Long id) {
        logger.info("Fetching expense category with ID: {}", id);
        ExpenseCategoryResponseDto category = expenseCategoryService.getCategoryById(id);
        logger.info("Fetched expense category: {}", category);
        return ResponseEntity.ok(category);
    }

    @PostMapping
    @Operation(summary = "Create new expense category")
    @ApiResponse(responseCode = "201", description = "Expense category created successfully")
    public ResponseEntity<ExpenseCategoryResponseDto> createCategory(
            @Valid @RequestBody ExpenseCategoryRequestDto requestDTO) {
        logger.info("Creating new expense category with request: {}", requestDTO);
        ExpenseCategoryResponseDto createdCategory = expenseCategoryService.createCategory(requestDTO);
        logger.info("Created new expense category: {}", createdCategory);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update expense category")
    @ApiResponse(responseCode = "200", description = "Expense category updated successfully")
    @ApiResponse(responseCode = "404", description = "Expense category not found")
    public ResponseEntity<ExpenseCategoryResponseDto> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseCategoryRequestDto requestDTO) {
        logger.info("Updating expense category with ID: {}, Request: {}", id, requestDTO);
        ExpenseCategoryResponseDto updatedCategory = expenseCategoryService.updateCategory(id, requestDTO);
        logger.info("Updated expense category: {}", updatedCategory);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete expense category")
    @ApiResponse(responseCode = "204", description = "Expense category deleted successfully")
    @ApiResponse(responseCode = "404", description = "Expense category not found")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        logger.info("Deleting expense category with ID: {}", id);
        expenseCategoryService.deleteCategory(id);
        logger.info("Deleted expense category with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}