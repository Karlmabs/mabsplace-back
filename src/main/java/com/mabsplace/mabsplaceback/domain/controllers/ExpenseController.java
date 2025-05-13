package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.expense.ExpenseRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.expense.ExpenseResponseDto;
import com.mabsplace.mabsplaceback.domain.services.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Expenses management endpoints")
public class ExpenseController {

    private final ExpenseService expenseService;

    private static final Logger logger = LoggerFactory.getLogger(ExpenseController.class);

    @PreAuthorize("@securityExpressionUtil.hasAnyRole(authentication, 'GET_EXPENSES')")
    @GetMapping
    @Operation(summary = "Get all expenses")
    @ApiResponse(responseCode = "200", description = "List of expenses retrieved successfully")
    public ResponseEntity<List<ExpenseResponseDto>> getAllExpenses() {
        logger.info("Fetching all expenses");
        List<ExpenseResponseDto> expenses = expenseService.getAllExpenses();
        logger.info("Fetched {} expenses", expenses.size());
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get expense by ID")
    @ApiResponse(responseCode = "200", description = "Expense retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Expense not found")
    public ResponseEntity<ExpenseResponseDto> getExpenseById(@PathVariable Long id) {
        logger.info("Fetching expense with ID: {}", id);
        ExpenseResponseDto expense = expenseService.getExpenseById(id);
        logger.info("Fetched expense: {}", expense);
        return ResponseEntity.ok(expense);
    }

    @GetMapping("/by-date-range")
    @Operation(summary = "Get expenses by date range")
    @ApiResponse(responseCode = "200", description = "List of expenses retrieved successfully")
    public ResponseEntity<List<ExpenseResponseDto>> getExpensesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        logger.info("Fetching expenses between dates {} and {}", startDate, endDate);
        List<ExpenseResponseDto> expenses = expenseService.getExpensesByDateRange(startDate, endDate);
        logger.info("Fetched {} expenses", expenses.size());
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/by-category/{categoryId}")
    @Operation(summary = "Get expenses by category")
    @ApiResponse(responseCode = "200", description = "List of expenses retrieved successfully")
    public ResponseEntity<List<ExpenseResponseDto>> getExpensesByCategory(@PathVariable Long categoryId) {
        logger.info("Fetching expenses for category ID: {}", categoryId);
        List<ExpenseResponseDto> expenses = expenseService.getExpensesByCategory(categoryId);
        logger.info("Fetched {} expenses for category ID: {}", expenses.size(), categoryId);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/by-user/{userId}")
    @Operation(summary = "Get expenses by user")
    @ApiResponse(responseCode = "200", description = "List of expenses retrieved successfully")
    public ResponseEntity<List<ExpenseResponseDto>> getExpensesByUser(@PathVariable Long userId) {
        logger.info("Fetching expenses for user ID: {}", userId);
        List<ExpenseResponseDto> expenses = expenseService.getExpensesByUser(userId);
        logger.info("Fetched {} expenses for user ID: {}", expenses.size(), userId);
        return ResponseEntity.ok(expenses);
    }

    @PostMapping
    @Operation(summary = "Create new expense")
    @ApiResponse(responseCode = "201", description = "Expense created successfully")
    public ResponseEntity<ExpenseResponseDto> createExpense(
            @Valid @RequestBody ExpenseRequestDto requestDTO,
            @RequestHeader("User-Id") Long userId) {
        logger.info("Creating expense for user ID: {}, Request: {}", userId, requestDTO);
        ExpenseResponseDto createdExpense = expenseService.createExpense(requestDTO, userId);
        logger.info("Expense created: {}", createdExpense);
        return new ResponseEntity<>(createdExpense, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update expense")
    @ApiResponse(responseCode = "200", description = "Expense updated successfully")
    @ApiResponse(responseCode = "404", description = "Expense not found")
    public ResponseEntity<ExpenseResponseDto> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequestDto requestDTO) {
        logger.info("Updating expense with ID: {}, Request: {}", id, requestDTO);
        ExpenseResponseDto updatedExpense = expenseService.updateExpense(id, requestDTO);
        logger.info("Updated expense: {}", updatedExpense);
        return ResponseEntity.ok(updatedExpense);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete expense")
    @ApiResponse(responseCode = "204", description = "Expense deleted successfully")
    @ApiResponse(responseCode = "404", description = "Expense not found")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        logger.info("Deleting expense with ID: {}", id);
        expenseService.deleteExpense(id);
        logger.info("Deleted expense successfully with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}