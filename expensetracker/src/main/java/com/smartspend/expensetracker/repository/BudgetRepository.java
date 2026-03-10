package com.smartspend.expensetracker.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartspend.expensetracker.model.Budget;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    
    Optional<Budget> findByIdAndUserId(Long id, Long userId);

    List<Budget> findByUserIdOrderByYearDescMonthDesc(Long userId);

    boolean existsByUserIdAndCategoryIdAndMonthAndYear(Long userId, Long categoryId, Integer month, Integer year);

    Optional<Budget> findByUserIdAndCategoryIdAndMonthAndYear(Long userId, Long categoryId, Integer month, Integer year);

    boolean existsByUserIdAndCategoryIdAndMonthAndYearAndIdNot(Long userId, Long categoryId, Integer month, Integer year, Long id);
}
