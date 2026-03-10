package com.smartspend.expensetracker.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartspend.expensetracker.dto.dashboard.CategoryBreakdownResponse;
import com.smartspend.expensetracker.dto.dashboard.DashboardSummaryResponse;
import com.smartspend.expensetracker.dto.dashboard.MonthlyTrendResponse;
import com.smartspend.expensetracker.service.dashboard.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    @GetMapping("/category-breakdown")
    public ResponseEntity<List<CategoryBreakdownResponse>> getCategoryBreakdown() {
        return ResponseEntity.ok(dashboardService.getCategoryBreakdown());
    }

    @GetMapping("/monthly-trend")
    public ResponseEntity<List<MonthlyTrendResponse>> getMonthlyTrend() {
        return ResponseEntity.ok(dashboardService.getMonthlyTrend());
    }
}
