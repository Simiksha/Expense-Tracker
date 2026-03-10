package com.smartspend.expensetracker.dto.report;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.smartspend.expensetracker.enums.TransactionType;

public record ReportFilterRequest(
    TransactionType type,

        Long categoryId,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate
) {
    
}
