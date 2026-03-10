package com.smartspend.expensetracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.smartspend.expensetracker.dto.category.CategoryRequest;
import com.smartspend.expensetracker.dto.category.CategoryResponse;
import com.smartspend.expensetracker.enums.Role;
import com.smartspend.expensetracker.exception.BadRequestException;
import com.smartspend.expensetracker.model.Category;
import com.smartspend.expensetracker.model.User;
import com.smartspend.expensetracker.repository.CategoryRepository;
import com.smartspend.expensetracker.repository.TransactionRepository;
import com.smartspend.expensetracker.service.category.CategoryServiceImpl;
import com.smartspend.expensetracker.service.user.UserService;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTest {
    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void createCategory_shouldCreateSuccessfully() {
        User user = User.builder()
                .id(1L)
                .name("testuser")
                .email("testuser@gmail.com")
                .password("encoded")
                .role(Role.ROLE_USER)
                .enabled(true)
                .emailVerified(true)
                .build();

        CategoryRequest request = new CategoryRequest("Food", "Food expenses");

        Category savedCategory = Category.builder()
                .id(10L)
                .name("Food")
                .description("Food expenses")
                .user(user)
                .build();

        given(userService.getCurrentUser()).willReturn(user);
        given(categoryRepository.existsByNameIgnoreCaseAndUserId("Food", 1L)).willReturn(false);
        given(categoryRepository.save(any(Category.class))).willReturn(savedCategory);

        CategoryResponse response = categoryService.createCategory(request);

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals("Food", response.name());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_shouldThrowException_whenDuplicateNameExists() {
        User user = User.builder()
                .id(1L)
                .build();

        CategoryRequest request = new CategoryRequest("Food", "Food expenses");

        given(userService.getCurrentUser()).willReturn(user);
        given(categoryRepository.existsByNameIgnoreCaseAndUserId("Food", 1L)).willReturn(true);

        assertThrows(BadRequestException.class, () -> categoryService.createCategory(request));

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void deleteCategory_shouldDelete_whenNotUsedInTransactions() {
        User user = User.builder().id(1L).build();

        Category category = Category.builder()
                .id(5L)
                .name("Food")
                .user(user)
                .build();

        given(userService.getCurrentUser()).willReturn(user);
        given(categoryRepository.findByIdAndUserId(5L, 1L)).willReturn(Optional.of(category));
        given(transactionRepository.existsByCategoryIdAndUserId(5L, 1L)).willReturn(false);

        categoryService.deleteCategory(5L);

        verify(categoryRepository).delete(category);
    }
}
