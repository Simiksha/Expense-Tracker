package com.smartspend.expensetracker.dto.dashboard;

import java.math.BigDecimal;

public record CategoryBreakdownResponse(
    Long categoryId,
    String categoryName,
    BigDecimal totalAmount
) {
    
}
