package com.smartspend.expensetracker.service.budget;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;

import com.smartspend.expensetracker.dto.budget.BudgetRequest;
import com.smartspend.expensetracker.dto.budget.BudgetResponse;
import com.smartspend.expensetracker.enums.BudgetStatus;
import com.smartspend.expensetracker.exception.BadRequestException;
import com.smartspend.expensetracker.exception.ResourceNotFoundException;
import com.smartspend.expensetracker.model.Budget;
import com.smartspend.expensetracker.model.Category;
import com.smartspend.expensetracker.model.User;
import com.smartspend.expensetracker.repository.BudgetRepository;
import com.smartspend.expensetracker.repository.CategoryRepository;
import com.smartspend.expensetracker.repository.TransactionRepository;
import com.smartspend.expensetracker.service.user.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {
    
    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserService userService;

    @Override
    public BudgetResponse createBudget(BudgetRequest request) {
        User currentUser = userService.getCurrentUser();

        Category category = categoryRepository.findByIdAndUserId(request.categoryId(), currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(
                currentUser.getId(), request.categoryId(), request.month(), request.year())) {
            throw new BadRequestException("Budget already exists for this category, month, and year");
        }

        Budget budget = Budget.builder()
                .category(category)
                .user(currentUser)
                .limitAmount(request.limitAmount())
                .month(request.month())
                .year(request.year())
                .build();

        return mapToResponse(budgetRepository.save(budget));
    }

    @Override
    public List<BudgetResponse> getMyBudgets() {
        User currentUser = userService.getCurrentUser();

        return budgetRepository.findByUserIdOrderByYearDescMonthDesc(currentUser.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public BudgetResponse getMyBudgetById(Long id) {
        User currentUser = userService.getCurrentUser();

        Budget budget = budgetRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        return mapToResponse(budget);
    }

    @Override
    public BudgetResponse updateBudget(Long id, BudgetRequest request) {
        User currentUser = userService.getCurrentUser();

        Budget budget = budgetRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        Category category = categoryRepository.findByIdAndUserId(request.categoryId(), currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYearAndIdNot(
                currentUser.getId(), request.categoryId(), request.month(), request.year(), id)) {
            throw new BadRequestException("Another budget already exists for this category, month, and year");
        }

        budget.setCategory(category);
        budget.setLimitAmount(request.limitAmount());
        budget.setMonth(request.month());
        budget.setYear(request.year());

        return mapToResponse(budgetRepository.save(budget));
    }

    @Override
    public void deleteBudget(Long id) {
        User currentUser = userService.getCurrentUser();

        Budget budget = budgetRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        budgetRepository.delete(budget);
    }

    private BudgetResponse mapToResponse(Budget budget) {
        BigDecimal spentAmount = transactionRepository.getTotalExpenseByCategoryAndMonth(
                budget.getUser().getId(),
                budget.getCategory().getId(),
                budget.getMonth(),
                budget.getYear()
        );

        BigDecimal remainingAmount = budget.getLimitAmount().subtract(spentAmount);

        BigDecimal usagePercentage = BigDecimal.ZERO;
        if (budget.getLimitAmount().compareTo(BigDecimal.ZERO) > 0) {
            usagePercentage = spentAmount
                    .multiply(BigDecimal.valueOf(100))
                    .divide(budget.getLimitAmount(), 2, RoundingMode.HALF_UP);
        }

        BudgetStatus status = getBudgetStatus(usagePercentage);

        return new BudgetResponse(
                budget.getId(),
                budget.getCategory().getId(),
                budget.getCategory().getName(),
                budget.getLimitAmount(),
                budget.getMonth(),
                budget.getYear(),
                spentAmount,
                remainingAmount,
                usagePercentage,
                status
        );
    }

    private BudgetStatus getBudgetStatus(BigDecimal usagePercentage) {
        if (usagePercentage.compareTo(BigDecimal.valueOf(100)) > 0) {
            return BudgetStatus.EXCEEDED;
        }
        if (usagePercentage.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return BudgetStatus.NEARING_LIMIT;
        }
        return BudgetStatus.SAFE;
    }
}
