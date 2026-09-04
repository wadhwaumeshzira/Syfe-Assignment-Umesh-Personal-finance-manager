package com.syfe.pfm.dto;

import java.util.List;

public class SavingsGoalListResponse {
    private List<SavingsGoalResponse> goals;

    public SavingsGoalListResponse() {
    }

    public SavingsGoalListResponse(List<SavingsGoalResponse> goals) {
        this.goals = goals;
    }

    public List<SavingsGoalResponse> getGoals() {
        return goals;
    }

    public void setGoals(List<SavingsGoalResponse> goals) {
        this.goals = goals;
    }
}
