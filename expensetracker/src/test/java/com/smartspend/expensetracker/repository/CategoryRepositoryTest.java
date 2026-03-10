package com.smartspend.expensetracker.repository;

import com.smartspend.expensetracker.model.Category;
import com.smartspend.expensetracker.model.User;
import com.smartspend.expensetracker.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("should find categories by user id ordered by name")
    void findByUserIdOrderByNameAsc_shouldReturnOrderedCategories() {
        User user = userRepository.save(buildUser("testuser@gmail.com"));

        categoryRepository.save(Category.builder()
                .name("Travel")
                .description("Travel expenses")
                .user(user)
                .build());

        categoryRepository.save(Category.builder()
                .name("Food")
                .description("Food expenses")
                .user(user)
                .build());

        List<Category> categories = categoryRepository.findByUserIdOrderByNameAsc(user.getId());

        assertEquals(2, categories.size());
        assertEquals("Food", categories.get(0).getName());
        assertEquals("Travel", categories.get(1).getName());
    }

    @Test
    @DisplayName("should find category by id and user id")
    void findByIdAndUserId_shouldReturnCategory() {
        User user = userRepository.save(buildUser("testuser2@gmail.com"));

        Category saved = categoryRepository.save(Category.builder()
                .name("Food")
                .description("Food expenses")
                .user(user)
                .build());

        Optional<Category> result = categoryRepository.findByIdAndUserId(saved.getId(), user.getId());

        assertTrue(result.isPresent());
        assertEquals("Food", result.get().getName());
    }

    @Test
    @DisplayName("should return true when category exists by name and user")
    void existsByNameIgnoreCaseAndUserId_shouldReturnTrue() {
        User user = userRepository.save(buildUser("testuser3@gmail.com"));

        categoryRepository.save(Category.builder()
                .name("Food")
                .description("Food expenses")
                .user(user)
                .build());

        boolean exists = categoryRepository.existsByNameIgnoreCaseAndUserId("food", user.getId());

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
}
