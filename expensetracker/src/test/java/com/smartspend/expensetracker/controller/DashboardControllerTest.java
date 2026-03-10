package com.smartspend.expensetracker.controller;

import com.smartspend.expensetracker.dto.dashboard.CategoryBreakdownResponse;
import com.smartspend.expensetracker.dto.dashboard.DashboardSummaryResponse;
import com.smartspend.expensetracker.dto.dashboard.MonthlyTrendResponse;
import com.smartspend.expensetracker.exception.GlobalExceptionHandler;
import com.smartspend.expensetracker.security.jwt.JwtService;
import com.smartspend.expensetracker.service.dashboard.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean 
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void getSummary_shouldReturnDashboardSummary() throws Exception {
        DashboardSummaryResponse response = new DashboardSummaryResponse(
                new BigDecimal("50000.00"),
                new BigDecimal("8500.00"),
                new BigDecimal("41500.00")
        );

        given(dashboardService.getSummary()).willReturn(response);

        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(50000.00))
                .andExpect(jsonPath("$.totalExpense").value(8500.00))
                .andExpect(jsonPath("$.balance").value(41500.00));
    }

    @Test
    void getCategoryBreakdown_shouldReturnCategoryBreakdownList() throws Exception {
        List<CategoryBreakdownResponse> responses = List.of(
                new CategoryBreakdownResponse(
                        1L,
                        "Food",
                        new BigDecimal("3500.00")
                ),
                new CategoryBreakdownResponse(
                        2L,
                        "Travel",
                        new BigDecimal("2000.00")
                )
        );

        given(dashboardService.getCategoryBreakdown()).willReturn(responses);

        mockMvc.perform(get("/api/dashboard/category-breakdown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].categoryId").value(1))
                .andExpect(jsonPath("$[0].categoryName").value("Food"))
                .andExpect(jsonPath("$[0].totalAmount").value(3500.00))
                .andExpect(jsonPath("$[1].categoryName").value("Travel"));
    }

    @Test
    void getMonthlyTrend_shouldReturnMonthlyTrendList() throws Exception {
        List<MonthlyTrendResponse> responses = List.of(
                new MonthlyTrendResponse(
                        2026,
                        1,
                        new BigDecimal("50000.00"),
                        new BigDecimal("7000.00")
                ),
                new MonthlyTrendResponse(
                        2026,
                        2,
                        new BigDecimal("50000.00"),
                        new BigDecimal("8500.00")
                )
        );

        given(dashboardService.getMonthlyTrend()).willReturn(responses);

        mockMvc.perform(get("/api/dashboard/monthly-trend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].year").value(2026))
                .andExpect(jsonPath("$[0].month").value(1))
                .andExpect(jsonPath("$[0].income").value(50000.00))
                .andExpect(jsonPath("$[0].expense").value(7000.00))
                .andExpect(jsonPath("$[1].month").value(2));
    }

    @Test
    void getCategoryBreakdown_shouldReturnEmptyList() throws Exception {
        given(dashboardService.getCategoryBreakdown()).willReturn(List.of());

        mockMvc.perform(get("/api/dashboard/category-breakdown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getMonthlyTrend_shouldReturnEmptyList() throws Exception {
        given(dashboardService.getMonthlyTrend()).willReturn(List.of());

        mockMvc.perform(get("/api/dashboard/monthly-trend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
