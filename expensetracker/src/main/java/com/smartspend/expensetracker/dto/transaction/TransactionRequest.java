package com.smartspend.expensetracker.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.smartspend.expensetracker.enums.TransactionType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TransactionRequest(
    @NotBlank(message = "Title is required")
        @Size(max = 150, message = "Title must not exceed 150 characters")
        String title,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Invalid amount format")
        BigDecimal amount,

        @NotNull(message = "Type is required")
        TransactionType type,

        @NotNull(message = "Transaction date is required")
        LocalDate transactionDate,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @NotNull(message = "Category id is required")
        Long categoryId
) {
    
}
