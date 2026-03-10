package com.smartspend.expensetracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.smartspend.expensetracker.dto.transaction.TransactionFilterRequest;
import com.smartspend.expensetracker.dto.transaction.TransactionRequest;
import com.smartspend.expensetracker.dto.transaction.TransactionResponse;
import com.smartspend.expensetracker.enums.Role;
import com.smartspend.expensetracker.enums.TransactionType;
import com.smartspend.expensetracker.exception.BadRequestException;
import com.smartspend.expensetracker.exception.ResourceNotFoundException;
import com.smartspend.expensetracker.model.Category;
import com.smartspend.expensetracker.model.Transaction;
import com.smartspend.expensetracker.model.User;
import com.smartspend.expensetracker.repository.CategoryRepository;
import com.smartspend.expensetracker.repository.TransactionRepository;
import com.smartspend.expensetracker.service.alert.BudgetAlertService;
import com.smartspend.expensetracker.service.transaction.TransactionServiceImpl;
import com.smartspend.expensetracker.service.user.UserService;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceImplTest {
    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserService userService;

    @Mock
    private BudgetAlertService budgetAlertService;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    void createTransaction_shouldCreateExpenseAndTriggerBudgetAlert() {
        User user = buildUser();
        Category category = buildCategory(user);

        TransactionRequest request = new TransactionRequest(
                "Lunch",
                new BigDecimal("150.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 3, 9),
                "Lunch with friends",
                1L
        );

        Transaction savedTransaction = Transaction.builder()
                .id(10L)
                .title("Lunch")
                .amount(new BigDecimal("150.00"))
                .type(TransactionType.EXPENSE)
                .transactionDate(LocalDate.of(2026, 3, 9))
                .description("Lunch with friends")
                .category(category)
                .user(user)
                .build();

        given(userService.getCurrentUser()).willReturn(user);
        given(categoryRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(category));
        given(transactionRepository.save(any(Transaction.class))).willReturn(savedTransaction);

        TransactionResponse response = transactionService.createTransaction(request);

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals("Lunch", response.title());
        assertEquals("Food", response.categoryName());

        verify(transactionRepository).save(any(Transaction.class));
        verify(budgetAlertService).checkAndCreateAlert(1L, 1L, 3, 2026);
    }

    @Test
    void createTransaction_shouldCreateIncomeWithoutTriggeringBudgetAlert() {
        User user = buildUser();
        Category category = buildCategory(user);

        TransactionRequest request = new TransactionRequest(
                "Salary",
                new BigDecimal("45000.00"),
                TransactionType.INCOME,
                LocalDate.of(2026, 3, 1),
                "Monthly salary",
                1L
        );

        Transaction savedTransaction = Transaction.builder()
                .id(11L)
                .title("Salary")
                .amount(new BigDecimal("45000.00"))
                .type(TransactionType.INCOME)
                .transactionDate(LocalDate.of(2026, 3, 1))
                .description("Monthly salary")
                .category(category)
                .user(user)
                .build();

        given(userService.getCurrentUser()).willReturn(user);
        given(categoryRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(category));
        given(transactionRepository.save(any(Transaction.class))).willReturn(savedTransaction);

        TransactionResponse response = transactionService.createTransaction(request);

        assertNotNull(response);
        assertEquals(TransactionType.INCOME, response.type());

        verify(transactionRepository).save(any(Transaction.class));
        verifyNoInteractions(budgetAlertService);
    }

    @Test
    void createTransaction_shouldThrowException_whenCategoryNotFound() {
        User user = buildUser();

        TransactionRequest request = new TransactionRequest(
                "Lunch",
                new BigDecimal("150.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 3, 9),
                "Lunch with friends",
                99L
        );

        given(userService.getCurrentUser()).willReturn(user);
        given(categoryRepository.findByIdAndUserId(99L, 1L)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.createTransaction(request));

        verify(transactionRepository, never()).save(any(Transaction.class));
        verifyNoInteractions(budgetAlertService);
    }

    @Test
    void getMyTransactions_shouldReturnFilteredTransactions() {
        User user = buildUser();
        Category category = buildCategory(user);

        Transaction transaction = Transaction.builder()
                .id(10L)
                .title("Lunch")
                .amount(new BigDecimal("150.00"))
                .type(TransactionType.EXPENSE)
                .transactionDate(LocalDate.of(2026, 3, 9))
                .description("Lunch with friends")
                .category(category)
                .user(user)
                .build();

        TransactionFilterRequest filter = new TransactionFilterRequest(
                "lunch",
                TransactionType.EXPENSE,
                1L,
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31),
                new BigDecimal("100"),
                new BigDecimal("500"),
                "transactionDate",
                "desc"
        );

        given(userService.getCurrentUser()).willReturn(user);
        given(transactionRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Transaction>>any(), any(Sort.class)))
                .willReturn(List.of(transaction));

        List<TransactionResponse> responses = transactionService.getMyTransactions(filter);

        assertEquals(1, responses.size());
        assertEquals("Lunch", responses.get(0).title());
        assertEquals("Food", responses.get(0).categoryName());

        verify(transactionRepository).findAll(org.mockito.ArgumentMatchers.<Specification<Transaction>>any(), any(Sort.class));
    }

    @Test
    void getMyTransactions_shouldThrowException_whenStartDateAfterEndDate() {
        TransactionFilterRequest filter = new TransactionFilterRequest(
                null,
                null,
                null,
                LocalDate.of(2026, 3, 31),
                LocalDate.of(2026, 3, 1),
                null,
                null,
                "transactionDate",
                "desc"
        );

        assertThrows(BadRequestException.class, () -> transactionService.getMyTransactions(filter));

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void updateTransaction_shouldUpdateAndTriggerBudgetChecks() {
        User user = buildUser();
        Category oldCategory = buildCategory(user);

        Category newCategory = Category.builder()
                .id(2L)
                .name("Travel")
                .description("Travel expenses")
                .user(user)
                .build();

        Transaction existingTransaction = Transaction.builder()
                .id(10L)
                .title("Lunch")
                .amount(new BigDecimal("150.00"))
                .type(TransactionType.EXPENSE)
                .transactionDate(LocalDate.of(2026, 3, 9))
                .description("Lunch with friends")
                .category(oldCategory)
                .user(user)
                .build();

        TransactionRequest request = new TransactionRequest(
                "Bus Ticket",
                new BigDecimal("200.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 4, 2),
                "Travel expense",
                2L
        );

        Transaction updatedTransaction = Transaction.builder()
                .id(10L)
                .title("Bus Ticket")
                .amount(new BigDecimal("200.00"))
                .type(TransactionType.EXPENSE)
                .transactionDate(LocalDate.of(2026, 4, 2))
                .description("Travel expense")
                .category(newCategory)
                .user(user)
                .build();

        given(userService.getCurrentUser()).willReturn(user);
        given(transactionRepository.findByIdAndUserId(10L, 1L)).willReturn(Optional.of(existingTransaction));
        given(categoryRepository.findByIdAndUserId(2L, 1L)).willReturn(Optional.of(newCategory));
        given(transactionRepository.save(any(Transaction.class))).willReturn(updatedTransaction);

        TransactionResponse response = transactionService.updateTransaction(10L, request);

        assertNotNull(response);
        assertEquals("Bus Ticket", response.title());
        assertEquals("Travel", response.categoryName());

        verify(budgetAlertService).checkAndCreateAlert(1L, 1L, 3, 2026);
        verify(budgetAlertService).checkAndCreateAlert(1L, 2L, 4, 2026);
    }

    @Test
    void deleteTransaction_shouldDeleteAndTriggerBudgetCheck_whenExpense() {
        User user = buildUser();
        Category category = buildCategory(user);

        Transaction transaction = Transaction.builder()
                .id(10L)
                .title("Lunch")
                .amount(new BigDecimal("150.00"))
                .type(TransactionType.EXPENSE)
                .transactionDate(LocalDate.of(2026, 3, 9))
                .description("Lunch with friends")
                .category(category)
                .user(user)
                .build();

        given(userService.getCurrentUser()).willReturn(user);
        given(transactionRepository.findByIdAndUserId(10L, 1L)).willReturn(Optional.of(transaction));

        transactionService.deleteTransaction(10L);

        verify(transactionRepository).delete(transaction);
        verify(budgetAlertService).checkAndCreateAlert(1L, 1L, 3, 2026);
    }

    @Test
    void deleteTransaction_shouldDeleteWithoutBudgetCheck_whenIncome() {
        User user = buildUser();
        Category category = buildCategory(user);

        Transaction transaction = Transaction.builder()
                .id(11L)
                .title("Salary")
                .amount(new BigDecimal("45000.00"))
                .type(TransactionType.INCOME)
                .transactionDate(LocalDate.of(2026, 3, 1))
                .description("Monthly salary")
                .category(category)
                .user(user)
                .build();

        given(userService.getCurrentUser()).willReturn(user);
        given(transactionRepository.findByIdAndUserId(11L, 1L)).willReturn(Optional.of(transaction));

        transactionService.deleteTransaction(11L);

        verify(transactionRepository).delete(transaction);
        verifyNoInteractions(budgetAlertService);
    }

    private User buildUser() {
        return User.builder()
                .id(1L)
                .name("testuser")
                .email("testuser@gmail.com")
                .password("encoded-password")
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
