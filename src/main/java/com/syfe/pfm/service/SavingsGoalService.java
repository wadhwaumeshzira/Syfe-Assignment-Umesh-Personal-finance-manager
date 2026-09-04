package com.syfe.pfm.service;

import com.syfe.pfm.exception.BadRequestException;
import com.syfe.pfm.exception.ForbiddenException;
import com.syfe.pfm.exception.ResourceNotFoundException;
import com.syfe.pfm.model.SavingsGoal;
import com.syfe.pfm.model.Transaction;
import com.syfe.pfm.repository.SavingsGoalRepository;
import com.syfe.pfm.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final TransactionRepository transactionRepository;

    public SavingsGoalService(SavingsGoalRepository savingsGoalRepository, TransactionRepository transactionRepository) {
        this.savingsGoalRepository = savingsGoalRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public SavingsGoal createGoal(String goalName, BigDecimal targetAmount, LocalDate targetDate, LocalDate startDate, Long userId) {
        if (goalName == null || goalName.trim().isEmpty()) {
            throw new BadRequestException("Goal name is required");
        }

        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Target amount must be a positive decimal value");
        }

        if (targetDate == null) {
            throw new BadRequestException("Target date is required");
        }

        if (!targetDate.isAfter(LocalDate.now())) {
            throw new BadRequestException("Target date must be a future date");
        }

        LocalDate start = (startDate != null) ? startDate : LocalDate.now();

        SavingsGoal goal = new SavingsGoal(userId, goalName, targetAmount, targetDate, start);
        return savingsGoalRepository.save(goal);
    }

    public List<SavingsGoal> getGoalsForUser(Long userId) {
        return savingsGoalRepository.findByUserId(userId);
    }

    public SavingsGoal getGoalForUser(Long id, Long userId) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found with ID: " + id));

        if (!goal.getUserId().equals(userId)) {
            throw new ForbiddenException("Access denied to this savings goal");
        }

        return goal;
    }

    @Transactional
    public SavingsGoal updateGoal(Long id, BigDecimal targetAmount, LocalDate targetDate, Long userId) {
        SavingsGoal goal = getGoalForUser(id, userId);

        if (targetAmount != null) {
            if (targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Target amount must be a positive decimal value");
            }
            goal.setTargetAmount(targetAmount);
        }

        if (targetDate != null) {
            if (!targetDate.isAfter(LocalDate.now())) {
                throw new BadRequestException("Target date must be a future date");
            }
            goal.setTargetDate(targetDate);
        }

        return savingsGoalRepository.save(goal);
    }

    @Transactional
    public void deleteGoal(Long id, Long userId) {
        SavingsGoal goal = getGoalForUser(id, userId);
        savingsGoalRepository.delete(goal);
    }

    public BigDecimal calculateCurrentProgress(SavingsGoal goal) {
        List<Transaction> transactions = transactionRepository.findByUserIdAndDateGreaterThanEqual(
                goal.getUserId(), 
                goal.getStartDate()
        );

        BigDecimal income = transactions.stream()
                .filter(t -> "INCOME".equalsIgnoreCase(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expense = transactions.stream()
                .filter(t -> "EXPENSE".equalsIgnoreCase(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return income.subtract(expense);
    }

    public BigDecimal calculateProgressPercentage(SavingsGoal goal, BigDecimal currentProgress) {
        if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }
        
        BigDecimal percentage = currentProgress
                .multiply(BigDecimal.valueOf(100))
                .divide(goal.getTargetAmount(), 2, RoundingMode.HALF_UP)
                .stripTrailingZeros();
        
        if (percentage.scale() < 1) {
            percentage = percentage.setScale(1, RoundingMode.HALF_UP);
        }
        
        return percentage;
    }

    public BigDecimal calculateRemainingAmount(SavingsGoal goal, BigDecimal currentProgress) {
        BigDecimal remaining = goal.getTargetAmount().subtract(currentProgress);
        // Requirement doesn't explicitly mention clamping remaining to 0 if goal is exceeded,
        // let's just subtract exactly as specified: "remaining amount = target amount - current progress"
        return remaining;
    }
}
