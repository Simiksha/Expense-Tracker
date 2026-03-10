package com.smartspend.expensetracker.service.category;

import java.util.List;

import org.springframework.stereotype.Service;

import com.smartspend.expensetracker.dto.category.CategoryRequest;
import com.smartspend.expensetracker.dto.category.CategoryResponse;
import com.smartspend.expensetracker.exception.BadRequestException;
import com.smartspend.expensetracker.exception.ResourceNotFoundException;
import com.smartspend.expensetracker.model.Category;
import com.smartspend.expensetracker.model.User;
import com.smartspend.expensetracker.repository.CategoryRepository;
import com.smartspend.expensetracker.repository.TransactionRepository;
import com.smartspend.expensetracker.service.user.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final UserService userService;
    private final TransactionRepository transactionRepository;

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        User currentUser = userService.getCurrentUser();

        if (categoryRepository.existsByNameIgnoreCaseAndUserId(request.name(), currentUser.getId())) {
            throw new BadRequestException("Category with this name already exists");
        }

        Category category = Category.builder()
                .name(request.name().trim())
                .description(request.description())
                .user(currentUser)
                .build();

        return mapToResponse(categoryRepository.save(category));
    }

    @Override
    public List<CategoryResponse> getMyCategories() {
        User currentUser = userService.getCurrentUser();

        return categoryRepository.findByUserIdOrderByNameAsc(currentUser.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public CategoryResponse getMyCategoryById(Long id) {
        User currentUser = userService.getCurrentUser();

        Category category = categoryRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        return mapToResponse(category);
    }

    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        User currentUser = userService.getCurrentUser();

        Category category = categoryRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (categoryRepository.existsByNameIgnoreCaseAndUserIdAndIdNot(request.name(), currentUser.getId(), id)) {
            throw new BadRequestException("Another category with this name already exists");
        }

        category.setName(request.name().trim());
        category.setDescription(request.description());

        return mapToResponse(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(Long id) {
        User currentUser = userService.getCurrentUser();

        Category category = categoryRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (transactionRepository.existsByCategoryIdAndUserId(id, currentUser.getId())) {
            throw new BadRequestException("Cannot delete category because it is used in transactions");
        }

        categoryRepository.delete(category);
    }

    private CategoryResponse mapToResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription());
    }
}
