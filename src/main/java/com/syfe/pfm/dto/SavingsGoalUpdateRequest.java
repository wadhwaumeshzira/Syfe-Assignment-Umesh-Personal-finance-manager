package com.syfe.pfm.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import java.math.BigDecimal;
import java.time.LocalDate;

public class SavingsGoalUpdateRequest {

    @DecimalMin(value = "0.01", message = "Target amount must be a positive value")
    private BigDecimal targetAmount;

    @Future(message = "Target date must be a future date")
    private LocalDate targetDate;

    public SavingsGoalUpdateRequest() {
    }

    public SavingsGoalUpdateRequest(BigDecimal targetAmount, LocalDate targetDate) {
        this.targetAmount = targetAmount;
        this.targetDate = targetDate;
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
}
