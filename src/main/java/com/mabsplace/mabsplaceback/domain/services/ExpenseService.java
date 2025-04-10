package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.expense.ExpenseRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.expense.ExpenseResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Expense;
import com.mabsplace.mabsplaceback.domain.entities.User;
import com.mabsplace.mabsplaceback.domain.enums.RecurrencePeriod;
import com.mabsplace.mabsplaceback.domain.mappers.ExpenseMapper;
import com.mabsplace.mabsplaceback.domain.repositories.CurrencyRepository;
import com.mabsplace.mabsplaceback.domain.repositories.ExpenseCategoryRepository;
import com.mabsplace.mabsplaceback.domain.repositories.ExpenseRepository;
import com.mabsplace.mabsplaceback.domain.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseService {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseService.class);

    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;
    private final UserRepository userRepository;
    private final ExpenseCategoryRepository categoryRepository;
    private final CurrencyRepository currencyRepository;

    public List<ExpenseResponseDto> getAllExpenses() {
        logger.info("Retrieving all expenses");
        List<ExpenseResponseDto> expenses = expenseRepository.findAll().stream()
                .map(expenseMapper::toResponseDTO)
                .collect(Collectors.toList());
        logger.info("Retrieved {} expenses", expenses.size());
        return expenses;
    }

    public List<ExpenseResponseDto> getExpensesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Retrieving expenses from {} to {}", startDate, endDate);
        List<ExpenseResponseDto> expenses = expenseRepository.findByExpenseDateBetween(startDate, endDate).stream()
                .map(expenseMapper::toResponseDTO)
                .collect(Collectors.toList());
        logger.info("Retrieved {} expenses for the specified date range", expenses.size());
        return expenses;
    }

    public ExpenseResponseDto getExpenseById(Long id) {
        logger.info("Retrieving expense with ID: {}", id);
        return expenseRepository.findById(id)
                .map(expenseMapper::toResponseDTO)
                .orElseThrow(() -> {
                    logger.error("Expense not found with ID: {}", id);
                    return new EntityNotFoundException("Expense not found with id: " + id);
                });
    }

    public ExpenseResponseDto createExpense(ExpenseRequestDto expenseRequestDto, Long userId) {
        logger.info("Creating expense with data: {}, user ID: {}", expenseRequestDto, userId);
        verifyRelatedEntities(expenseRequestDto);

        Expense expense = expenseMapper.toEntity(expenseRequestDto);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        expense.setCreatedBy(user);

        // Add this block to handle recurring expenses
        if (expense.isRecurring()) {
            if (expense.getRecurrencePeriod() == null) {
                throw new IllegalArgumentException("Recurrence period must be specified for recurring expenses");
            }
            // Set the next recurrence date based on the expense date
            expense.setNextRecurrenceDate(
                calculateNextRecurrenceDate(expense.getExpenseDate(), expense.getRecurrencePeriod())
            );
        }

        Expense savedExpense = expenseRepository.save(expense);
        logger.info("Expense created successfully: {}", expenseMapper.toResponseDTO(savedExpense));
        return expenseMapper.toResponseDTO(savedExpense);
    }

    public ExpenseResponseDto updateExpense(Long id, ExpenseRequestDto requestDTO) {
        logger.info("Updating expense with ID: {}, Request: {}", id, requestDTO);
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Expense not found with ID: {}", id);
                    return new EntityNotFoundException("Expense not found with id: " + id);
                });

        verifyRelatedEntities(requestDTO);

        expenseMapper.updateEntityFromDTO(requestDTO, expense);
        Expense updatedExpense = expenseRepository.save(expense);
        logger.info("Expense updated successfully: {}", expenseMapper.toResponseDTO(updatedExpense));
        return expenseMapper.toResponseDTO(updatedExpense);
    }

    public void deleteExpense(Long id) {
        logger.info("Deleting expense with ID: {}", id);
        if (!expenseRepository.existsById(id)) {
            logger.error("Expense not found with ID: {}", id);
            throw new EntityNotFoundException("Expense not found with id: " + id);
        }
        expenseRepository.deleteById(id);
        logger.info("Expense deleted successfully with ID: {}", id);
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
        logger.info("Retrieving expenses by category ID: {}", categoryId);
        List<ExpenseResponseDto> expenses = expenseRepository.findByCategoryId(categoryId).stream()
                .map(expenseMapper::toResponseDTO)
                .collect(Collectors.toList());
        logger.info("Retrieved {} expenses for category ID: {}", expenses.size(), categoryId);
        return expenses;
    }

    public List<ExpenseResponseDto> getExpensesByUser(Long userId) {
        logger.info("Retrieving expenses created by user ID: {}", userId);
        List<ExpenseResponseDto> expenses = expenseRepository.findByCreatedById(userId).stream()
                .map(expenseMapper::toResponseDTO)
                .collect(Collectors.toList());
        logger.info("Retrieved {} expenses created by user ID: {}", expenses.size(), userId);
        return expenses;
    }

    @Scheduled(cron = "0 0 0 * * *") // Runs daily at midnight
    public void processRecurringExpenses() {
        logger.info("Processing recurring expenses");
        List<Expense> recurringExpenses = expenseRepository
            .findByIsRecurringTrueAndNextRecurrenceDateLessThanEqual(LocalDateTime.now());

        for (Expense expense : recurringExpenses) {
            try {
                createRecurringExpense(expense);
                updateNextRecurrenceDate(expense);
            } catch (Exception e) {
                logger.error("Failed to process recurring expense ID: {}", expense.getId(), e);
            }
        }
    }

    private void createRecurringExpense(Expense originalExpense) {
        Expense newExpense = new Expense();
        newExpense.setCategory(originalExpense.getCategory());
        newExpense.setAmount(originalExpense.getAmount());
        newExpense.setDescription(originalExpense.getDescription());
        newExpense.setExpenseDate(LocalDateTime.now());
        newExpense.setPaymentMethod(originalExpense.getPaymentMethod());
        newExpense.setCurrency(originalExpense.getCurrency());
        newExpense.setCreatedBy(originalExpense.getCreatedBy());
        newExpense.setRecurring(false); // This is an instance, not the template

        expenseRepository.save(newExpense);
        logger.info("Created recurring expense instance from template ID: {}", originalExpense.getId());
    }

    private void updateNextRecurrenceDate(Expense expense) {
        LocalDateTime nextDate = calculateNextRecurrenceDate(
            expense.getNextRecurrenceDate(),
            expense.getRecurrencePeriod()
        );
        expense.setNextRecurrenceDate(nextDate);
        expenseRepository.save(expense);
        logger.info("Updated next recurrence date for expense ID: {} to {}",
            expense.getId(), nextDate);
    }

    private LocalDateTime calculateNextRecurrenceDate(LocalDateTime currentDate, RecurrencePeriod period) {
        return switch (period) {
            case DAILY -> currentDate.plusDays(1);
            case WEEKLY -> currentDate.plusWeeks(1);
            case MONTHLY -> currentDate.plusMonths(1);
            case YEARLY -> currentDate.plusYears(1);
        };
    }
}
