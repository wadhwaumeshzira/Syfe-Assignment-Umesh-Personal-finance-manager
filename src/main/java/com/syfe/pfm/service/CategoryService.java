package com.syfe.pfm.service;

import com.syfe.pfm.exception.ConflictException;
import com.syfe.pfm.exception.ForbiddenException;
import com.syfe.pfm.exception.ResourceNotFoundException;
import com.syfe.pfm.model.Category;
import com.syfe.pfm.repository.CategoryRepository;
import com.syfe.pfm.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public CategoryService(CategoryRepository categoryRepository, TransactionRepository transactionRepository) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<Category> getAllCategories(Long userId) {
        return categoryRepository.findAllByUserIdOrSystem(userId);
    }

    @Transactional
    public Category createCustomCategory(String name, String type, Long userId) {
        // Validate type
        if (!"INCOME".equalsIgnoreCase(type) && !"EXPENSE".equalsIgnoreCase(type)) {
            throw new IllegalArgumentException("Category type must be INCOME or EXPENSE");
        }

        // Check if category name already exists (either system-wide or user's custom)
        if (categoryRepository.existsByNameAndUserIdOrSystem(name, userId)) {
            throw new ConflictException("Category '" + name + "' already exists");
        }

        Category category = new Category(name, type.toUpperCase(), true, userId);
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCustomCategory(String name, Long userId) {
        Category category = categoryRepository.findByNameAndUserIdOrSystem(name, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + name));

        // Default categories cannot be deleted
        if (!category.isCustom()) {
            throw new ForbiddenException("Default categories cannot be deleted");
        }

        // Categories referenced by transactions cannot be deleted
        if (transactionRepository.existsByUserIdAndCategoryName(userId, name)) {
            throw new ConflictException("Category '" + name + "' is referenced by transactions and cannot be deleted");
        }

        categoryRepository.delete(category);
    }
}
