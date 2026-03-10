package com.smartspend.expensetracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.smartspend.expensetracker.dto.budget.BudgetRequest;
import com.smartspend.expensetracker.dto.budget.BudgetResponse;
import com.smartspend.expensetracker.enums.BudgetStatus;
import com.smartspend.expensetracker.enums.Role;
import com.smartspend.expensetracker.exception.BadRequestException;
import com.smartspend.expensetracker.exception.ResourceNotFoundException;
import com.smartspend.expensetracker.model.Budget;
import com.smartspend.expensetracker.model.Category;
import com.smartspend.expensetracker.model.User;
import com.smartspend.expensetracker.repository.BudgetRepository;
import com.smartspend.expensetracker.repository.CategoryRepository;
import com.smartspend.expensetracker.repository.TransactionRepository;
import com.smartspend.expensetracker.service.budget.BudgetServiceImpl;
import com.smartspend.expensetracker.service.user.UserService;

@ExtendWith(MockitoExtension.class)
public class BudgetServiceImplTest {
    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    @Test
    void createBudget_shouldCreateSuccessfully() {
        User user = buildUser();
        Category category = buildCategory(user);

        BudgetRequest request = new BudgetRequest(
                1L,
                new BigDecimal("5000"),
                3,
                2026
        );

        Budget savedBudget = Budget.builder()
                .id(10L)
                .limitAmount(new BigDecimal("5000"))
                .month(3)
                .year(2026)
                .category(category)
                .user(user)
                .build();

        given(userService.getCurrentUser()).willReturn(user);
        given(categoryRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(category));
        given(budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(1L, 1L, 3, 2026))
                .willReturn(false);
        given(budgetRepository.save(any(Budget.class))).willReturn(savedBudget);

        given(transactionRepository.getTotalExpenseByCategoryAndMonth(1L, 1L, 3, 2026))
                .willReturn(BigDecimal.ZERO);

        BudgetResponse response = budgetService.createBudget(request);

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals("Food", response.categoryName());
        assertEquals(BudgetStatus.SAFE, response.status());

        verify(budgetRepository).save(any(Budget.class));
    }

    @Test
    void createBudget_shouldThrowException_whenDuplicateBudgetExists() {
        User user = buildUser();
        Category category = buildCategory(user);

        BudgetRequest request = new BudgetRequest(
                1L,
                new BigDecimal("5000"),
                3,
                2026
        );

        given(userService.getCurrentUser()).willReturn(user);
        given(categoryRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(category));
        given(budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(1L, 1L, 3, 2026))
                .willReturn(true);

        assertThrows(BadRequestException.class, () -> budgetService.createBudget(request));

        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void getMyBudgets_shouldReturnBudgets() {
        User user = buildUser();
        Category category = buildCategory(user);

        Budget budget = Budget.builder()
                .id(1L)
                .limitAmount(new BigDecimal("5000"))
                .month(3)
                .year(2026)
                .category(category)
                .user(user)
                .build();

        given(userService.getCurrentUser()).willReturn(user);
        given(budgetRepository.findByUserIdOrderByYearDescMonthDesc(1L))
                .willReturn(List.of(budget));

        given(transactionRepository.getTotalExpenseByCategoryAndMonth(1L, 1L, 3, 2026))
                .willReturn(new BigDecimal("1000"));

        List<BudgetResponse> responses = budgetService.getMyBudgets();

        assertEquals(1, responses.size());
        assertEquals("Food", responses.get(0).categoryName());
        assertEquals(new BigDecimal("1000"), responses.get(0).spentAmount());
    }

    @Test
    void deleteBudget_shouldDeleteSuccessfully() {
        User user = buildUser();
        Category category = buildCategory(user);

        Budget budget = Budget.builder()
                .id(5L)
                .category(category)
                .user(user)
                .limitAmount(new BigDecimal("5000"))
                .month(3)
                .year(2026)
                .build();

        given(userService.getCurrentUser()).willReturn(user);
        given(budgetRepository.findByIdAndUserId(5L, 1L))
                .willReturn(Optional.of(budget));

        budgetService.deleteBudget(5L);

        verify(budgetRepository).delete(budget);
    }

    @Test
    void getMyBudgetById_shouldThrowException_whenNotFound() {
        User user = buildUser();

        given(userService.getCurrentUser()).willReturn(user);
        given(budgetRepository.findByIdAndUserId(99L, 1L))
                .willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> budgetService.getMyBudgetById(99L));
    }

    private User buildUser() {
        return User.builder()
                .id(1L)
                .name("testuser")
                .email("testuser@gmail.com")
                .password("encoded")
                .role(Role.ROLE_USER)
                .enabled(true)
                .emailVerified(true)
                .build();
    }

    private Category buildCategory(User user) {
        return Category.builder()
                .id(1L)
                .name("Food")
                .description("Food expenses")
                .user(user)
                .build();
    }
}
