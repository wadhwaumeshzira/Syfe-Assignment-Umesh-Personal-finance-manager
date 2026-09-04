package com.syfe.pfm.service;

import com.syfe.pfm.exception.BadRequestException;
import com.syfe.pfm.exception.ForbiddenException;
import com.syfe.pfm.model.SavingsGoal;
import com.syfe.pfm.model.Transaction;
import com.syfe.pfm.repository.SavingsGoalRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavingsGoalServiceTest {

    @Mock
    private SavingsGoalRepository savingsGoalRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private SavingsGoalService savingsGoalService;

    private Long userId;

    @BeforeEach
    void setUp() {
        userId = 1L;
    }

    @Test
    void createGoal_ShouldSaveAndReturn_WhenValid() {
        String name = "Emergency Fund";
        BigDecimal target = BigDecimal.valueOf(5000.0);
        LocalDate targetDate = LocalDate.now().plusMonths(6);
        LocalDate start = LocalDate.now();

        when(savingsGoalRepository.save(any(SavingsGoal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SavingsGoal created = savingsGoalService.createGoal(name, target, targetDate, start, userId);

        assertNotNull(created);
        assertEquals(name, created.getGoalName());
        assertEquals(target, created.getTargetAmount());
        assertEquals(targetDate, created.getTargetDate());
        assertEquals(start, created.getStartDate());
    }

    @Test
    void createGoal_ShouldThrowBadRequest_WhenTargetAmountIsNegativeOrZero() {
        assertThrows(BadRequestException.class, () -> 
                savingsGoalService.createGoal("Car", BigDecimal.valueOf(-5.0), LocalDate.now().plusDays(5), LocalDate.now(), userId)
        );
        assertThrows(BadRequestException.class, () -> 
                savingsGoalService.createGoal("Car", BigDecimal.ZERO, LocalDate.now().plusDays(5), LocalDate.now(), userId)
        );
    }

    @Test
    void createGoal_ShouldThrowBadRequest_WhenTargetDateIsNotInFuture() {
        assertThrows(BadRequestException.class, () -> 
                savingsGoalService.createGoal("Car", BigDecimal.valueOf(100.0), LocalDate.now(), LocalDate.now(), userId)
        );
        assertThrows(BadRequestException.class, () -> 
                savingsGoalService.createGoal("Car", BigDecimal.valueOf(100.0), LocalDate.now().minusDays(1), LocalDate.now(), userId)
        );
    }

    @Test
    void calculateCurrentProgress_ShouldReturnNetSavingsSinceStartDate() {
        SavingsGoal goal = new SavingsGoal(userId, "Fund", BigDecimal.valueOf(1000.0), LocalDate.now().plusMonths(1), LocalDate.now().minusDays(10));

        Transaction t1 = new Transaction(userId, BigDecimal.valueOf(1500.0), LocalDate.now().minusDays(5), "Salary", "Paycheck", "INCOME");
        Transaction t2 = new Transaction(userId, BigDecimal.valueOf(400.0), LocalDate.now().minusDays(2), "Food", "Dinner", "EXPENSE");
        Transaction t3 = new Transaction(userId, BigDecimal.valueOf(100.0), LocalDate.now().minusDays(1), "Rent", "Utility", "EXPENSE");

        when(transactionRepository.findByUserIdAndDateGreaterThanEqual(userId, goal.getStartDate()))
                .thenReturn(Arrays.asList(t1, t2, t3));

        BigDecimal progress = savingsGoalService.calculateCurrentProgress(goal);

        // progress = 1500 - (400 + 100) = 1000
        assertEquals(BigDecimal.valueOf(1000.0), progress);
    }

    @Test
    void calculateProgressPercentage_ShouldRoundToTwoDecimalPlaces() {
        SavingsGoal goal = new SavingsGoal(userId, "Fund", BigDecimal.valueOf(6000.0), LocalDate.now().plusMonths(1), LocalDate.now());

        BigDecimal progress = BigDecimal.valueOf(1000.0);
        BigDecimal percentage = savingsGoalService.calculateProgressPercentage(goal, progress);

        // 1000 / 6000 * 100 = 16.666... rounded to 2 decimal places = 16.67
        assertEquals(BigDecimal.valueOf(16.67), percentage);
    }

    @Test
    void calculateRemainingAmount_ShouldReturnCorrectDifference() {
        SavingsGoal goal = new SavingsGoal(userId, "Fund", BigDecimal.valueOf(5000.0), LocalDate.now().plusMonths(1), LocalDate.now());
        BigDecimal progress = BigDecimal.valueOf(1200.0);

        BigDecimal remaining = savingsGoalService.calculateRemainingAmount(goal, progress);

        assertEquals(BigDecimal.valueOf(3800.0), remaining);
    }
}
