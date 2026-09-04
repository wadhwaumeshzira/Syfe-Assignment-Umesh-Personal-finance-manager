package com.syfe.pfm.dto;

import java.util.List;

public class TransactionListResponse {
    private List<TransactionResponse> transactions;

    public TransactionListResponse() {
    }

    public TransactionListResponse(List<TransactionResponse> transactions) {
        this.transactions = transactions;
    }

    public List<TransactionResponse> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionResponse> transactions) {
        this.transactions = transactions;
    }
}
