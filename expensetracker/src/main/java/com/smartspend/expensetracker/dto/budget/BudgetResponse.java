package com.smartspend.expensetracker.dto.budget;

import java.math.BigDecimal;

import com.smartspend.expensetracker.enums.BudgetStatus;

public record BudgetResponse(
    Long id,
    Long categoryId,
    String categoryName,
    BigDecimal limitAmount,
    Integer month,
    Integer year,
    BigDecimal spentAmount,
    BigDecimal remainingAmount,
    BigDecimal usagePercentage,
    BudgetStatus status
) {
    
}
