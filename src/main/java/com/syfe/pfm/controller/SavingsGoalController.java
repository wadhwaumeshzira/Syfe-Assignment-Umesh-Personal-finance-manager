package com.syfe.pfm.controller;

import com.syfe.pfm.dto.*;
import com.syfe.pfm.model.SavingsGoal;
import com.syfe.pfm.security.UserPrincipal;
import com.syfe.pfm.service.SavingsGoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller handling savings goals management.
 */
@RestController
@RequestMapping("/api/goals")
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    public SavingsGoalController(SavingsGoalService savingsGoalService) {
        this.savingsGoalService = savingsGoalService;
    }

    @PostMapping
    public ResponseEntity<SavingsGoalResponse> createGoal(
            @Valid @RequestBody SavingsGoalRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        SavingsGoal goal = savingsGoalService.createGoal(
                request.getGoalName(),
                request.getTargetAmount(),
                request.getTargetDate(),
                request.getStartDate(),
                principal.getId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(buildResponse(goal));
    }

    @GetMapping
    public ResponseEntity<SavingsGoalListResponse> getGoals(@AuthenticationPrincipal UserPrincipal principal) {
        List<SavingsGoal> goals = savingsGoalService.getGoalsForUser(principal.getId());
        List<SavingsGoalResponse> responses = goals.stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new SavingsGoalListResponse(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SavingsGoalResponse> getGoal(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        SavingsGoal goal = savingsGoalService.getGoalForUser(id, principal.getId());
        return ResponseEntity.ok(buildResponse(goal));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SavingsGoalResponse> updateGoal(
            @PathVariable Long id,
            @Valid @RequestBody SavingsGoalUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        SavingsGoal goal = savingsGoalService.updateGoal(
                id,
                request.getTargetAmount(),
                request.getTargetDate(),
                principal.getId()
        );
        return ResponseEntity.ok(buildResponse(goal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteGoal(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        savingsGoalService.deleteGoal(id, principal.getId());
        return ResponseEntity.ok(new MessageResponse("Goal deleted successfully"));
    }

    private SavingsGoalResponse buildResponse(SavingsGoal goal) {
        BigDecimal progress = savingsGoalService.calculateCurrentProgress(goal);
        BigDecimal percentage = savingsGoalService.calculateProgressPercentage(goal, progress);
        BigDecimal remaining = savingsGoalService.calculateRemainingAmount(goal, progress);
        return new SavingsGoalResponse(goal, progress, percentage, remaining);
    }
}
