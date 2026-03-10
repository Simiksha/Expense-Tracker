package com.smartspend.expensetracker.service.budget;

import java.util.List;

import com.smartspend.expensetracker.dto.budget.BudgetRequest;
import com.smartspend.expensetracker.dto.budget.BudgetResponse;

public interface BudgetService {
    BudgetResponse createBudget(BudgetRequest request);
    List<BudgetResponse> getMyBudgets();
    BudgetResponse getMyBudgetById(Long id);
    BudgetResponse updateBudget(Long id, BudgetRequest request);
    void deleteBudget(Long id);
}
