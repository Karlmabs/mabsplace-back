package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.expenseCategory.ExpenseCategoryRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.expenseCategory.ExpenseCategoryResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.ExpenseCategory;
import com.mabsplace.mabsplaceback.domain.mappers.ExpenseCategoryMapper;
import com.mabsplace.mabsplaceback.domain.repositories.ExpenseCategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
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

    public List<ExpenseCategoryResponseDto> getAllCategories() {
        return expenseCategoryRepository.findAll().stream()
                .map(expenseCategoryMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public ExpenseCategoryResponseDto getCategoryById(Long id) {
        return expenseCategoryRepository.findById(id)
                .map(expenseCategoryMapper::toResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException("ExpenseCategory not found with id: " + id));
    }

    public ExpenseCategoryResponseDto createCategory(ExpenseCategoryRequestDto requestDTO) {
        ExpenseCategory category = expenseCategoryMapper.toEntity(requestDTO);
        ExpenseCategory savedCategory = expenseCategoryRepository.save(category);
        return expenseCategoryMapper.toResponseDTO(savedCategory);
    }

    public ExpenseCategoryResponseDto updateCategory(Long id, ExpenseCategoryRequestDto requestDTO) {
        ExpenseCategory category = expenseCategoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ExpenseCategory not found with id: " + id));

        expenseCategoryMapper.updateEntityFromDTO(requestDTO, category);
        ExpenseCategory updatedCategory = expenseCategoryRepository.save(category);
        return expenseCategoryMapper.toResponseDTO(updatedCategory);
    }

    public void deleteCategory(Long id) {
        if (!expenseCategoryRepository.existsById(id)) {
            throw new EntityNotFoundException("ExpenseCategory not found with id: " + id);
        }
        expenseCategoryRepository.deleteById(id);
    }
}
