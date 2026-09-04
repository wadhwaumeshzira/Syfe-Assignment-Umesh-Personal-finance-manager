package com.syfe.pfm.service;

import com.syfe.pfm.exception.BadRequestException;
import com.syfe.pfm.exception.ForbiddenException;
import com.syfe.pfm.exception.ResourceNotFoundException;
import com.syfe.pfm.model.Category;
import com.syfe.pfm.model.Transaction;
import com.syfe.pfm.repository.CategoryRepository;
import com.syfe.pfm.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    public TransactionService(TransactionRepository transactionRepository, CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Transaction createTransaction(BigDecimal amount, LocalDate date, String categoryName, String description, Long userId) {
        // Validate amount
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Transaction amount must be a positive decimal value");
        }

        // Validate date
        if (date == null) {
            throw new BadRequestException("Transaction date is required");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new BadRequestException("Transaction date cannot be in the future");
        }

        // Validate category
        Category category = categoryRepository.findByNameAndUserIdOrSystem(categoryName, userId)
                .orElseThrow(() -> new BadRequestException("Category '" + categoryName + "' does not exist or is not accessible"));

        Transaction transaction = new Transaction(
                userId,
                amount,
                date,
                category.getName(),
                description,
                category.getType()
        );

        return transactionRepository.save(transaction);
    }

    public List<Transaction> getFilteredTransactions(Long userId, LocalDate startDate, LocalDate endDate, String category, String type) {
        return transactionRepository.findFilteredTransactions(userId, startDate, endDate, category, type);
    }

    @Transactional
    public Transaction updateTransaction(Long id, BigDecimal amount, String categoryName, String description, Long userId) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + id));

        // Data segregation check
        if (!transaction.getUserId().equals(userId)) {
            throw new ForbiddenException("Access denied to this transaction data");
        }

        // If amount is provided, validate and update
        if (amount != null) {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Transaction amount must be a positive decimal value");
            }
            transaction.setAmount(amount);
        }

        // If category is provided, validate and update
        if (categoryName != null && !categoryName.trim().isEmpty()) {
            Category category = categoryRepository.findByNameAndUserIdOrSystem(categoryName, userId)
                    .orElseThrow(() -> new BadRequestException("Category '" + categoryName + "' does not exist or is not accessible"));
            transaction.setCategoryName(category.getName());
            transaction.setType(category.getType());
        }

        // If description is provided, update (could be null/empty)
        if (description != null) {
            transaction.setDescription(description);
        }

        // NOTE: Date field is deliberately not modified as per business rules.

        return transactionRepository.save(transaction);
    }

    @Transactional
    public void deleteTransaction(Long id, Long userId) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + id));

        // Data segregation check
        if (!transaction.getUserId().equals(userId)) {
            throw new ForbiddenException("Access denied to this transaction data");
        }

        transactionRepository.delete(transaction);
    }
}
