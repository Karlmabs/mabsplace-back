package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.expense.ExpenseRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.expense.ExpenseResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Expense;
import com.mabsplace.mabsplaceback.domain.entities.User;
import com.mabsplace.mabsplaceback.domain.mappers.ExpenseMapper;
import com.mabsplace.mabsplaceback.domain.repositories.CurrencyRepository;
import com.mabsplace.mabsplaceback.domain.repositories.ExpenseCategoryRepository;
import com.mabsplace.mabsplaceback.domain.repositories.ExpenseRepository;
import com.mabsplace.mabsplaceback.domain.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;
    private final UserRepository userRepository;
    private final ExpenseCategoryRepository categoryRepository;
    private final CurrencyRepository currencyRepository;

    public Page<ExpenseResponseDto> getAllExpenses(Pageable pageable) {
        return expenseRepository.findAll(pageable)
                .map(expenseMapper::toResponseDTO);
    }

    public List<ExpenseResponseDto> getExpensesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return expenseRepository.findByExpenseDateBetween(startDate, endDate).stream()
                .map(expenseMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public ExpenseResponseDto getExpenseById(Long id) {
        return expenseRepository.findById(id)
                .map(expenseMapper::toResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found with id: " + id));
    }

    public ExpenseResponseDto createExpense(ExpenseRequestDto requestDTO, Long userId) {
        // Verify all required entities exist
        verifyRelatedEntities(requestDTO);

        Expense expense = expenseMapper.toEntity(requestDTO);

        // Set the created by user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        expense.setCreatedBy(user);

        Expense savedExpense = expenseRepository.save(expense);
        return expenseMapper.toResponseDTO(savedExpense);
    }

    public ExpenseResponseDto updateExpense(Long id, ExpenseRequestDto requestDTO) {
        // Verify all required entities exist
        verifyRelatedEntities(requestDTO);

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found with id: " + id));

        expenseMapper.updateEntityFromDTO(requestDTO, expense);
        Expense updatedExpense = expenseRepository.save(expense);
        return expenseMapper.toResponseDTO(updatedExpense);
    }

    public void deleteExpense(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new EntityNotFoundException("Expense not found with id: " + id);
        }
        expenseRepository.deleteById(id);
    }

    private void verifyRelatedEntities(ExpenseRequestDto requestDTO) {
        // Verify category exists
        categoryRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + requestDTO.getCategoryId()));

        // Verify currency exists
        currencyRepository.findById(requestDTO.getCurrencyId())
                .orElseThrow(() -> new EntityNotFoundException("Currency not found with id: " + requestDTO.getCurrencyId()));
    }

    public List<ExpenseResponseDto> getExpensesByCategory(Long categoryId) {
        return expenseRepository.findByCategoryId(categoryId).stream()
                .map(expenseMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ExpenseResponseDto> getExpensesByUser(Long userId) {
        return expenseRepository.findByCreatedById(userId).stream()
                .map(expenseMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}
