package com.syfe.pfm.config;

import com.syfe.pfm.model.Category;
import com.syfe.pfm.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Component to seed the database with predefined default categories on application startup.
 */
@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    public DatabaseSeeder(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        // System default categories
        List<Category> defaultCategories = Arrays.asList(
                new Category("Salary", "INCOME", false, null),
                new Category("Food", "EXPENSE", false, null),
                new Category("Rent", "EXPENSE", false, null),
                new Category("Transportation", "EXPENSE", false, null),
                new Category("Entertainment", "EXPENSE", false, null),
                new Category("Healthcare", "EXPENSE", false, null),
                new Category("Utilities", "EXPENSE", false, null)
        );

        for (Category defaultCat : defaultCategories) {
            // Check if it already exists by name and system level (userId is null)
            boolean exists = categoryRepository.findByNameAndUserIdOrSystem(defaultCat.getName(), null)
                    .stream()
                    .anyMatch(c -> !c.isCustom());
            
            if (!exists) {
                categoryRepository.save(defaultCat);
            }
        }
    }
}
