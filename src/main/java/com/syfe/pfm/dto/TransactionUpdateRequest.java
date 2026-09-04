package com.syfe.pfm.dto;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public class TransactionUpdateRequest {

    @DecimalMin(value = "0.01", message = "Amount must be a positive value")
    private BigDecimal amount;

    private String category;

    private String description;

    public TransactionUpdateRequest() {
    }

    public TransactionUpdateRequest(BigDecimal amount, String category, String description) {
        this.amount = amount;
        this.category = category;
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
