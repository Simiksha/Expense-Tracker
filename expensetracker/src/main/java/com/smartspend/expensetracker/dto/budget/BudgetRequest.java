package com.smartspend.expensetracker.dto.budget;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BudgetRequest(
    @NotNull(message = "Category id is required")
    Long categoryId,

    @NotNull(message = "Limit amount is required")
    @DecimalMin(value = "0.01", message = "Budget amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Invalid budget amount")
    BigDecimal limitAmount,

    @NotNull(message = "Month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    Integer month,

    @NotNull(message = "Year is required")
    @Min(value = 2000, message = "Year is invalid")
    Integer year
) {
    
}
