package com.smartspend.expensetracker.dto.dashboard;

import java.math.BigDecimal;

public record MonthlyTrendResponse(
    Integer year,
    Integer month,
    BigDecimal income,
    BigDecimal expense
) {
    
}
