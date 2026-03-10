package com.smartspend.expensetracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartspend.expensetracker.dto.transaction.TransactionRequest;
import com.smartspend.expensetracker.dto.transaction.TransactionResponse;
import com.smartspend.expensetracker.enums.TransactionType;
import com.smartspend.expensetracker.exception.GlobalExceptionHandler;
import com.smartspend.expensetracker.security.jwt.JwtAuthenticationFilter;
import com.smartspend.expensetracker.service.transaction.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private UserDetailsService userDetailsService;


    @Test
    void createTransaction_shouldReturnCreated() throws Exception {
        TransactionRequest request = new TransactionRequest(
                "Lunch",
                new BigDecimal("150.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 3, 9),
                "Lunch with friends",
                1L
        );

        TransactionResponse response = new TransactionResponse(
                1L,
                "Lunch",
                new BigDecimal("150.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 3, 9),
                "Lunch with friends",
                1L,
                "Food"
        );

        given(transactionService.createTransaction(any(TransactionRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Lunch"))
                .andExpect(jsonPath("$.categoryName").value("Food"));
    }

    @Test
    void getMyTransactions_shouldReturnList() throws Exception {
        List<TransactionResponse> responses = List.of(
                new TransactionResponse(
                        1L, "Lunch", new BigDecimal("150.00"), TransactionType.EXPENSE,
                        LocalDate.of(2026, 3, 9), "Lunch", 1L, "Food"
                )
        );

        given(transactionService.getMyTransactions(any())).willReturn(responses);

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Lunch"));
    }

    @Test
    void getMyTransactionById_shouldReturnTransaction() throws Exception {
        TransactionResponse response = new TransactionResponse(
                1L,
                "Lunch",
                new BigDecimal("150.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 3, 9),
                "Lunch",
                1L,
                "Food"
        );

        given(transactionService.getMyTransactionById(1L)).willReturn(response);

        mockMvc.perform(get("/api/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Lunch"));
    }

    @Test
    void deleteTransaction_shouldReturnSuccessMessage() throws Exception {
        mockMvc.perform(delete("/api/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transaction deleted successfully"));
    }
}
