package com.smartspend.expensetracker.service.category;

import java.util.List;

import com.smartspend.expensetracker.dto.category.CategoryRequest;
import com.smartspend.expensetracker.dto.category.CategoryResponse;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest request);
    List<CategoryResponse> getMyCategories();
    CategoryResponse getMyCategoryById(Long id);
    CategoryResponse updateCategory(Long id, CategoryRequest request);
    void deleteCategory(Long id);
}
