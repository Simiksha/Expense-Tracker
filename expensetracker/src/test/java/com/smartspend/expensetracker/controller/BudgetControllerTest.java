package com.smartspend.expensetracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartspend.expensetracker.dto.budget.BudgetRequest;
import com.smartspend.expensetracker.dto.budget.BudgetResponse;
import com.smartspend.expensetracker.enums.BudgetStatus;
import com.smartspend.expensetracker.exception.GlobalExceptionHandler;
import com.smartspend.expensetracker.security.jwt.JwtService;
import com.smartspend.expensetracker.service.budget.BudgetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BudgetController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private BudgetService budgetService;

    @MockitoBean 
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void createBudget_shouldReturnCreated() throws Exception {
        BudgetRequest request = new BudgetRequest(
                1L,
                new BigDecimal("5000"),
                3,
                2026
        );

        BudgetResponse response = new BudgetResponse(
                1L,
                1L,
                "Food",
                new BigDecimal("5000"),
                3,
                2026,
                new BigDecimal("1200"),
                new BigDecimal("3800"),
                new BigDecimal("24.00"),
                BudgetStatus.SAFE
        );

        given(budgetService.createBudget(any(BudgetRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.categoryName").value("Food"))
                .andExpect(jsonPath("$.status").value("SAFE"));
    }

    @Test
    void getMyBudgets_shouldReturnList() throws Exception {
        List<BudgetResponse> responses = List.of(
                new BudgetResponse(
                        1L, 1L, "Food",
                        new BigDecimal("5000"),
                        3, 2026,
                        new BigDecimal("1200"),
                        new BigDecimal("3800"),
                        new BigDecimal("24.00"),
                        BudgetStatus.SAFE
                )
        );

        given(budgetService.getMyBudgets()).willReturn(responses);

        mockMvc.perform(get("/api/budgets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].categoryName").value("Food"));
    }

    @Test
    void getMyBudgetById_shouldReturnBudget() throws Exception {
        BudgetResponse response = new BudgetResponse(
                1L,
                1L,
                "Food",
                new BigDecimal("5000"),
                3,
                2026,
                new BigDecimal("1200"),
                new BigDecimal("3800"),
                new BigDecimal("24.00"),
                BudgetStatus.SAFE
        );

        given(budgetService.getMyBudgetById(1L)).willReturn(response);

        mockMvc.perform(get("/api/budgets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.categoryName").value("Food"));
    }

    @Test
    void deleteBudget_shouldReturnSuccessMessage() throws Exception {
        mockMvc.perform(delete("/api/budgets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Budget deleted successfully"));
    }

    @Test
    void createBudget_shouldReturnBadRequest_whenInvalidInput() throws Exception {
        BudgetRequest request = new BudgetRequest(
                null,
                new BigDecimal("0"),
                13,
                1999
        );

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
