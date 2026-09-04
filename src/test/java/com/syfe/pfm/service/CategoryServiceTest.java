package com.syfe.pfm.service;

import com.syfe.pfm.exception.ConflictException;
import com.syfe.pfm.exception.ForbiddenException;
import com.syfe.pfm.exception.ResourceNotFoundException;
import com.syfe.pfm.model.Category;
import com.syfe.pfm.repository.CategoryRepository;
import com.syfe.pfm.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Long userId;

    @BeforeEach
    void setUp() {
        userId = 1L;
    }

    @Test
    void getAllCategories_ShouldReturnAllCategories() {
        Category systemCat = new Category("Food", "EXPENSE", false, null);
        Category customCat = new Category("Bonus", "INCOME", true, userId);
        
        when(categoryRepository.findAllByUserIdOrSystem(userId))
                .thenReturn(Arrays.asList(systemCat, customCat));

        List<Category> result = categoryService.getAllCategories(userId);

        assertEquals(2, result.size());
        assertEquals("Food", result.get(0).getName());
        assertEquals("Bonus", result.get(1).getName());
    }

    @Test
    void createCustomCategory_ShouldSaveAndReturn_WhenNameIsUnique() {
        String catName = "Freelance";
        String catType = "INCOME";

        when(categoryRepository.existsByNameAndUserIdOrSystem(catName, userId)).thenReturn(false);
        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Category created = categoryService.createCustomCategory(catName, catType, userId);

        assertNotNull(created);
        assertEquals(catName, created.getName());
        assertEquals(catType, created.getType());
        assertTrue(created.isCustom());
        assertEquals(userId, created.getUserId());
    }

    @Test
    void createCustomCategory_ShouldThrowConflict_WhenNameAlreadyExists() {
        String catName = "Food";
        String catType = "EXPENSE";

        when(categoryRepository.existsByNameAndUserIdOrSystem(catName, userId)).thenReturn(true);

        assertThrows(ConflictException.class, () -> 
                categoryService.createCustomCategory(catName, catType, userId)
        );
    }

    @Test
    void deleteCustomCategory_ShouldDelete_WhenValidAndNotReferenced() {
        String catName = "Freelance";
        Category customCat = new Category(catName, "INCOME", true, userId);

        when(categoryRepository.findByNameAndUserIdOrSystem(catName, userId))
                .thenReturn(Optional.of(customCat));
        when(transactionRepository.existsByUserIdAndCategoryName(userId, catName))
                .thenReturn(false);

        categoryService.deleteCustomCategory(catName, userId);

        verify(categoryRepository, times(1)).delete(customCat);
    }

    @Test
    void deleteCustomCategory_ShouldThrowForbidden_WhenItIsDefault() {
        String catName = "Food";
        Category systemCat = new Category(catName, "EXPENSE", false, null);

        when(categoryRepository.findByNameAndUserIdOrSystem(catName, userId))
                .thenReturn(Optional.of(systemCat));

        assertThrows(ForbiddenException.class, () -> 
                categoryService.deleteCustomCategory(catName, userId)
        );
    }

    @Test
    void deleteCustomCategory_ShouldThrowConflict_WhenReferencedByTransactions() {
        String catName = "Freelance";
        Category customCat = new Category(catName, "INCOME", true, userId);

        when(categoryRepository.findByNameAndUserIdOrSystem(catName, userId))
                .thenReturn(Optional.of(customCat));
        when(transactionRepository.existsByUserIdAndCategoryName(userId, catName))
                .thenReturn(true);

        assertThrows(ConflictException.class, () -> 
                categoryService.deleteCustomCategory(catName, userId)
        );
    }

    @Test
    void deleteCustomCategory_ShouldThrowNotFound_WhenDoesNotExist() {
        String catName = "Unknown";

        when(categoryRepository.findByNameAndUserIdOrSystem(catName, userId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
                categoryService.deleteCustomCategory(catName, userId)
        );
    }
}
