package com.smartspend.expensetracker.dto.dashboard;

import java.math.BigDecimal;

public record DashboardSummaryResponse(
    BigDecimal totalIncome,
    BigDecimal totalExpense,
    BigDecimal balance
) {
    
}
