package com.smartspend.expensetracker.repository;

import com.smartspend.expensetracker.model.Category;
import com.smartspend.expensetracker.model.Transaction;
import com.smartspend.expensetracker.model.User;
import com.smartspend.expensetracker.enums.Role;
import com.smartspend.expensetracker.enums.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("should find transaction by id and user id")
    void findByIdAndUserId_shouldReturnTransaction() {
        User user = userRepository.save(buildUser("testuser1@gmail.com"));
        Category category = categoryRepository.save(buildCategory(user, "Food"));

        Transaction saved = transactionRepository.save(Transaction.builder()
                .title("Lunch")
                .amount(new BigDecimal("150.00"))
                .type(TransactionType.EXPENSE)
                .transactionDate(LocalDate.of(2026, 3, 9))
                .description("Lunch")
                .category(category)
                .user(user)
                .build());

        Optional<Transaction> result = transactionRepository.findByIdAndUserId(saved.getId(), user.getId());

        assertTrue(result.isPresent());
        assertEquals("Lunch", result.get().getTitle());
    }

    @Test
    @DisplayName("should calculate total income")
    void getTotalIncome_shouldReturnCorrectSum() {
        User user = userRepository.save(buildUser("testuser2@gmail.com"));
        Category category = categoryRepository.save(buildCategory(user, "Salary"));

        transactionRepository.save(Transaction.builder()
                .title("Salary 1")
                .amount(new BigDecimal("30000.00"))
                .type(TransactionType.INCOME)
                .transactionDate(LocalDate.of(2026, 3, 1))
                .category(category)
                .user(user)
                .build());

        transactionRepository.save(Transaction.builder()
                .title("Salary 2")
                .amount(new BigDecimal("20000.00"))
                .type(TransactionType.INCOME)
                .transactionDate(LocalDate.of(2026, 3, 10))
                .category(category)
                .user(user)
                .build());

        BigDecimal totalIncome = transactionRepository.getTotalIncome(user.getId());

        assertEquals(new BigDecimal("50000.00"), totalIncome);
    }

    @Test
    @DisplayName("should calculate total expense by category and month")
    void getTotalExpenseByCategoryAndMonth_shouldReturnCorrectSum() {
        User user = userRepository.save(buildUser("testuser3@gmail.com"));
        Category category = categoryRepository.save(buildCategory(user, "Food"));

        transactionRepository.save(Transaction.builder()
                .title("Lunch")
                .amount(new BigDecimal("150.00"))
                .type(TransactionType.EXPENSE)
                .transactionDate(LocalDate.of(2026, 3, 9))
                .category(category)
                .user(user)
                .build());

        transactionRepository.save(Transaction.builder()
                .title("Dinner")
                .amount(new BigDecimal("250.00"))
                .type(TransactionType.EXPENSE)
                .transactionDate(LocalDate.of(2026, 3, 10))
                .category(category)
                .user(user)
                .build());

        BigDecimal total = transactionRepository.getTotalExpenseByCategoryAndMonth(
                user.getId(), category.getId(), 3, 2026
        );

        assertEquals(new BigDecimal("400.00"), total);
    }

    @Test
    @DisplayName("should check if transaction exists by category and user")
    void existsByCategoryIdAndUserId_shouldReturnTrue() {
        User user = userRepository.save(buildUser("testuser4@gmail.com"));
        Category category = categoryRepository.save(buildCategory(user, "Food"));

        transactionRepository.save(Transaction.builder()
                .title("Lunch")
                .amount(new BigDecimal("150.00"))
                .type(TransactionType.EXPENSE)
                .transactionDate(LocalDate.of(2026, 3, 9))
                .category(category)
                .user(user)
                .build());

        boolean exists = transactionRepository.existsByCategoryIdAndUserId(category.getId(), user.getId());

        assertTrue(exists);
    }

    private User buildUser(String email) {
        return User.builder()
                .name("testuser")
                .email(email)
                .password("encoded-password")
                .role(Role.ROLE_USER)
                .enabled(true)
                .emailVerified(true)
                .build();
    }

    private Category buildCategory(User user, String name) {
        return Category.builder()
                .name(name)
                .description(name + " expenses")
                .user(user)
                .build();
    }
}
