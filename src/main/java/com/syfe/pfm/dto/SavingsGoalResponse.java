package com.syfe.pfm.dto;

import com.syfe.pfm.model.SavingsGoal;
import java.math.BigDecimal;
import java.time.LocalDate;

public class SavingsGoalResponse {
    private Long id;
    private String goalName;
    private BigDecimal targetAmount;
    private LocalDate targetDate;
    private LocalDate startDate;
    private BigDecimal currentProgress;
    private BigDecimal progressPercentage;
    private BigDecimal remainingAmount;

    public SavingsGoalResponse() {
    }

    public SavingsGoalResponse(SavingsGoal goal, BigDecimal currentProgress, BigDecimal progressPercentage, BigDecimal remainingAmount) {
        this.id = goal.getId();
        this.goalName = goal.getGoalName();
        this.targetAmount = goal.getTargetAmount();
        this.targetDate = goal.getTargetDate();
        this.startDate = goal.getStartDate();
        this.currentProgress = currentProgress;
        this.progressPercentage = progressPercentage;
        this.remainingAmount = remainingAmount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGoalName() {
        return goalName;
    }

    public void setGoalName(String goalName) {
        this.goalName = goalName;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public BigDecimal getCurrentProgress() {
        return currentProgress;
    }

    public void setCurrentProgress(BigDecimal currentProgress) {
        this.currentProgress = currentProgress;
    }

    public BigDecimal getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(BigDecimal progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }
}
