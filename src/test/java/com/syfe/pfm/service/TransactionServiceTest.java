package com.syfe.pfm.service;

import com.syfe.pfm.exception.BadRequestException;
import com.syfe.pfm.exception.ForbiddenException;
import com.syfe.pfm.exception.ResourceNotFoundException;
import com.syfe.pfm.model.Category;
import com.syfe.pfm.model.Transaction;
import com.syfe.pfm.repository.CategoryRepository;
import com.syfe.pfm.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Long userId;

    @BeforeEach
    void setUp() {
        userId = 1L;
    }

    @Test
    void createTransaction_ShouldSaveAndReturn_WhenValid() {
        BigDecimal amount = BigDecimal.valueOf(100.0);
        LocalDate date = LocalDate.now();
        String catName = "Food";
        String desc = "Dinner";
        Category category = new Category(catName, "EXPENSE", false, null);

        when(categoryRepository.findByNameAndUserIdOrSystem(catName, userId))
                .thenReturn(Optional.of(category));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.createTransaction(amount, date, catName, desc, userId);

        assertNotNull(result);
        assertEquals(amount, result.getAmount());
        assertEquals(date, result.getDate());
        assertEquals(catName, result.getCategoryName());
        assertEquals("EXPENSE", result.getType());
        assertEquals(userId, result.getUserId());
    }

    @Test
    void createTransaction_ShouldThrowBadRequest_WhenAmountIsNegativeOrZero() {
        assertThrows(BadRequestException.class, () -> 
                transactionService.createTransaction(BigDecimal.valueOf(-10.0), LocalDate.now(), "Food", "Dinner", userId)
        );
        assertThrows(BadRequestException.class, () -> 
                transactionService.createTransaction(BigDecimal.ZERO, LocalDate.now(), "Food", "Dinner", userId)
        );
    }

    @Test
    void createTransaction_ShouldThrowBadRequest_WhenDateIsInFuture() {
        assertThrows(BadRequestException.class, () -> 
                transactionService.createTransaction(BigDecimal.valueOf(10.0), LocalDate.now().plusDays(1), "Food", "Dinner", userId)
        );
    }

    @Test
    void createTransaction_ShouldThrowBadRequest_WhenCategoryDoesNotExist() {
        when(categoryRepository.findByNameAndUserIdOrSystem("Unknown", userId))
                .thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> 
                transactionService.createTransaction(BigDecimal.valueOf(10.0), LocalDate.now(), "Unknown", "Dinner", userId)
        );
    }

    @Test
    void getFilteredTransactions_ShouldReturnList() {
        LocalDate start = LocalDate.now().minusDays(5);
        LocalDate end = LocalDate.now();
        Transaction t = new Transaction(userId, BigDecimal.valueOf(50.0), LocalDate.now(), "Food", "Lunch", "EXPENSE");
        
        when(transactionRepository.findFilteredTransactions(userId, start, end, "Food", "EXPENSE"))
                .thenReturn(Arrays.asList(t));

        List<Transaction> result = transactionService.getFilteredTransactions(userId, start, end, "Food", "EXPENSE");

        assertEquals(1, result.size());
        assertEquals("Lunch", result.get(0).getDescription());
    }

    @Test
    void updateTransaction_ShouldModifyFieldsExceptDate_WhenValid() {
        Long txId = 100L;
        LocalDate originalDate = LocalDate.now().minusDays(2);
        Transaction existing = new Transaction(userId, BigDecimal.valueOf(10.0), originalDate, "Food", "Lunch", "EXPENSE");
        existing.setId(txId);

        Category newCategory = new Category("Rent", "EXPENSE", false, null);

        when(transactionRepository.findById(txId)).thenReturn(Optional.of(existing));
        when(categoryRepository.findByNameAndUserIdOrSystem("Rent", userId)).thenReturn(Optional.of(newCategory));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction updated = transactionService.updateTransaction(txId, BigDecimal.valueOf(500.0), "Rent", "Monthly rent payment", userId);

        assertNotNull(updated);
        assertEquals(BigDecimal.valueOf(500.0), updated.getAmount());
        assertEquals("Rent", updated.getCategoryName());
        assertEquals("Monthly rent payment", updated.getDescription());
        // Date MUST remain unchanged
        assertEquals(originalDate, updated.getDate());
    }

    @Test
    void updateTransaction_ShouldThrowForbidden_WhenBelongsToAnotherUser() {
        Long txId = 100L;
        Transaction existing = new Transaction(99L, BigDecimal.valueOf(10.0), LocalDate.now(), "Food", "Lunch", "EXPENSE");
        existing.setId(txId);

        when(transactionRepository.findById(txId)).thenReturn(Optional.of(existing));

        assertThrows(ForbiddenException.class, () -> 
                transactionService.updateTransaction(txId, BigDecimal.valueOf(20.0), null, null, userId)
        );
    }

    @Test
    void deleteTransaction_ShouldDelete_WhenAuthorized() {
        Long txId = 100L;
        Transaction existing = new Transaction(userId, BigDecimal.valueOf(10.0), LocalDate.now(), "Food", "Lunch", "EXPENSE");
        existing.setId(txId);

        when(transactionRepository.findById(txId)).thenReturn(Optional.of(existing));

        transactionService.deleteTransaction(txId, userId);

        verify(transactionRepository, times(1)).delete(existing);
    }

    @Test
    void deleteTransaction_ShouldThrowForbidden_WhenUnauthorized() {
        Long txId = 100L;
        Transaction existing = new Transaction(99L, BigDecimal.valueOf(10.0), LocalDate.now(), "Food", "Lunch", "EXPENSE");
        existing.setId(txId);

        when(transactionRepository.findById(txId)).thenReturn(Optional.of(existing));

        assertThrows(ForbiddenException.class, () -> 
                transactionService.deleteTransaction(txId, userId)
        );
    }
}
