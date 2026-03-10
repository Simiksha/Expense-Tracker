package com.smartspend.expensetracker.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.smartspend.expensetracker.enums.TransactionType;

public record TransactionResponse(
    Long id,
    String title,
    BigDecimal amount,
    TransactionType type,
    LocalDate transactionDate,
    String description,
    Long categoryId,
    String categoryName
) {
    
}
