package com.syfe.pfm.controller;

import com.syfe.pfm.dto.*;
import com.syfe.pfm.model.Transaction;
import com.syfe.pfm.security.UserPrincipal;
import com.syfe.pfm.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller handling transaction CRUD and filtering endpoints.
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody TransactionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        Transaction transaction = transactionService.createTransaction(
                request.getAmount(),
                request.getDate(),
                request.getCategory(),
                request.getDescription(),
                principal.getId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(new TransactionResponse(transaction));
    }

    @GetMapping
    public ResponseEntity<TransactionListResponse> getTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        List<Transaction> transactions = transactionService.getFilteredTransactions(
                principal.getId(),
                startDate,
                endDate,
                category,
                type
        );
        List<TransactionResponse> responses = transactions.stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new TransactionListResponse(responses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        Transaction transaction = transactionService.updateTransaction(
                id,
                request.getAmount(),
                request.getCategory(),
                request.getDescription(),
                principal.getId()
        );
        return ResponseEntity.ok(new TransactionResponse(transaction));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteTransaction(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        transactionService.deleteTransaction(id, principal.getId());
        return ResponseEntity.ok(new MessageResponse("Transaction deleted successfully"));
    }
}
