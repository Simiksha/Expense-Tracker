package com.smartspend.expensetracker.service.dashboard;

import java.util.List;

import com.smartspend.expensetracker.dto.dashboard.CategoryBreakdownResponse;
import com.smartspend.expensetracker.dto.dashboard.DashboardSummaryResponse;
import com.smartspend.expensetracker.dto.dashboard.MonthlyTrendResponse;

public interface DashboardService {
    DashboardSummaryResponse getSummary();

    List<CategoryBreakdownResponse> getCategoryBreakdown();

    List<MonthlyTrendResponse> getMonthlyTrend();

}
