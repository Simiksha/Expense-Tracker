package com.smartspend.expensetracker.repository;

import com.smartspend.expensetracker.model.Budget;
import com.smartspend.expensetracker.model.Category;
import com.smartspend.expensetracker.model.User;
import com.smartspend.expensetracker.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BudgetRepositoryTest {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("should find budget by id and user id")
    void findByIdAndUserId_shouldReturnBudget() {
        User user = userRepository.save(buildUser("testuser1@gmail.com"));
        Category category = categoryRepository.save(buildCategory(user));

        Budget saved = budgetRepository.save(Budget.builder()
                .limitAmount(new BigDecimal("5000"))
                .month(3)
                .year(2026)
                .category(category)
                .user(user)
                .build());

        Optional<Budget> result = budgetRepository.findByIdAndUserId(saved.getId(), user.getId());

        assertTrue(result.isPresent());
        assertEquals(new BigDecimal("5000"), result.get().getLimitAmount());
    }

    @Test
    @DisplayName("should return true when budget exists for user category month year")
    void existsByUserIdAndCategoryIdAndMonthAndYear_shouldReturnTrue() {
        User user = userRepository.save(buildUser("testuser2@gmail.com"));
        Category category = categoryRepository.save(buildCategory(user));

        budgetRepository.save(Budget.builder()
                .limitAmount(new BigDecimal("5000"))
                .month(3)
                .year(2026)
                .category(category)
                .user(user)
                .build());

        boolean exists = budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(
                user.getId(), category.getId(), 3, 2026
        );

        assertTrue(exists);
    }

    @Test
    @DisplayName("should find budget by user category month year")
    void findByUserIdAndCategoryIdAndMonthAndYear_shouldReturnBudget() {
        User user = userRepository.save(buildUser("testuser3@gmail.com"));
        Category category = categoryRepository.save(buildCategory(user));

        budgetRepository.save(Budget.builder()
                .limitAmount(new BigDecimal("5000"))
                .month(3)
                .year(2026)
                .category(category)
                .user(user)
                .build());

        Optional<Budget> result = budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(
                user.getId(), category.getId(), 3, 2026
        );

        assertTrue(result.isPresent());
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

    private Category buildCategory(User user) {
        return Category.builder()
                .name("Food")
                .description("Food expenses")
                .user(user)
                .build();
    }
}
