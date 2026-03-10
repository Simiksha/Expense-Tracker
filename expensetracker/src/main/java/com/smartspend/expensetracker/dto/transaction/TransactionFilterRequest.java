package com.smartspend.expensetracker.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.smartspend.expensetracker.enums.TransactionType;

public record TransactionFilterRequest(
    String keyword,
    TransactionType type,
    Long categoryId,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate startDate,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate endDate,

    BigDecimal minAmount,
    BigDecimal maxAmount,

    String sortBy,
    String sortDir
) {
    
}
