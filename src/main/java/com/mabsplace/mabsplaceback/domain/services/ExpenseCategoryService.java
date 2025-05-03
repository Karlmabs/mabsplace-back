package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.expenseCategory.ExpenseCategoryRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.expenseCategory.ExpenseCategoryResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.ExpenseCategory;
import com.mabsplace.mabsplaceback.domain.mappers.ExpenseCategoryMapper;
import com.mabsplace.mabsplaceback.domain.repositories.ExpenseCategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseCategoryService {

    private final ExpenseCategoryRepository expenseCategoryRepository;
    private final ExpenseCategoryMapper expenseCategoryMapper;
    private static final Logger logger = LoggerFactory.getLogger(ExpenseCategoryService.class);

    public List<ExpenseCategoryResponseDto> getAllCategories() {
        logger.info("Retrieving all expense categories");
        List<ExpenseCategoryResponseDto> categories = expenseCategoryRepository.findAll().stream()
                .map(expenseCategoryMapper::toResponseDTO)
                .collect(Collectors.toList());
        logger.info("Retrieved {} expense categories", categories.size());
        return categories;
    }

    public ExpenseCategoryResponseDto getCategoryById(Long id) {
        logger.info("Retrieving expense category with ID: {}", id);
        return expenseCategoryRepository.findById(id)
                .map(expenseCategoryMapper::toResponseDTO)
                .orElseThrow(() -> {
                    logger.error("ExpenseCategory not found with ID: {}", id);
                    return new EntityNotFoundException("ExpenseCategory not found with id: " + id);
                });
    }

    public ExpenseCategoryResponseDto createCategory(ExpenseCategoryRequestDto requestDTO) {
        logger.info("Creating expense category with data: {}", requestDTO);
        ExpenseCategory category = expenseCategoryMapper.toEntity(requestDTO);
        ExpenseCategory savedCategory = expenseCategoryRepository.save(category);
        logger.info("Expense category created successfully: {}", expenseCategoryMapper.toResponseDTO(savedCategory));
        return expenseCategoryMapper.toResponseDTO(savedCategory);
    }

    public ExpenseCategoryResponseDto updateCategory(Long id, ExpenseCategoryRequestDto requestDTO) {
        logger.info("Updating expense category with ID: {} and data: {}", id, requestDTO);
        ExpenseCategory category = expenseCategoryRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("ExpenseCategory not found with ID: {}", id);
                    return new EntityNotFoundException("ExpenseCategory not found with id: " + id);
                });

        expenseCategoryMapper.updateEntityFromDTO(requestDTO, category);
        ExpenseCategory updatedCategory = expenseCategoryRepository.save(category);
        logger.info("Expense category updated successfully: {}", expenseCategoryMapper.toResponseDTO(updatedCategory));
        return expenseCategoryMapper.toResponseDTO(updatedCategory);
    }

    public void deleteCategory(Long id) {
        logger.info("Deleting expense category with ID: {}", id);
        if (!expenseCategoryRepository.existsById(id)) {
            logger.error("ExpenseCategory not found with ID: {}", id);
            throw new EntityNotFoundException("ExpenseCategory not found with id: " + id);
        }
        expenseCategoryRepository.deleteById(id);
        logger.info("Expense category deleted successfully with ID: {}", id);
    }

    public Long getCategoryByName(String referralRewards) {
        return expenseCategoryRepository.findByName(referralRewards).getId();
    }
}
