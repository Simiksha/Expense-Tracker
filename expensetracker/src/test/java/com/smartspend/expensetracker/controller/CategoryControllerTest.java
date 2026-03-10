package com.smartspend.expensetracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartspend.expensetracker.dto.category.CategoryRequest;
import com.smartspend.expensetracker.dto.category.CategoryResponse;
import com.smartspend.expensetracker.exception.GlobalExceptionHandler;
import com.smartspend.expensetracker.security.jwt.JwtAuthenticationFilter;
import com.smartspend.expensetracker.service.category.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void createCategory_shouldReturnCreated() throws Exception {
        CategoryRequest request = new CategoryRequest("Food", "Food expenses");

        CategoryResponse response = new CategoryResponse(
                1L,
                "Food",
                "Food expenses"
        );

        given(categoryService.createCategory(any(CategoryRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Food"))
                .andExpect(jsonPath("$.description").value("Food expenses"));
    }

    @Test
    void getMyCategories_shouldReturnList() throws Exception {
        List<CategoryResponse> responses = List.of(
                new CategoryResponse(1L, "Food", "Food expenses"),
                new CategoryResponse(2L, "Travel", "Travel expenses")
        );

        given(categoryService.getMyCategories()).willReturn(responses);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Food"))
                .andExpect(jsonPath("$[1].name").value("Travel"));
    }

    @Test
    void getMyCategoryById_shouldReturnCategory() throws Exception {
        CategoryResponse response = new CategoryResponse(
                1L,
                "Food",
                "Food expenses"
        );

        given(categoryService.getMyCategoryById(1L)).willReturn(response);

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Food"));
    }

    @Test
    void deleteCategory_shouldReturnSuccessMessage() throws Exception {
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Category deleted successfully"));
    }

    @Test
    void createCategory_shouldReturnBadRequest_whenInvalidInput() throws Exception {
        CategoryRequest request = new CategoryRequest("", "Food expenses");

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
