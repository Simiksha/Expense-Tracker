package com.smartspend.expensetracker.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.smartspend.expensetracker.model.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
        Optional<Transaction> findByIdAndUserId(Long id, Long userId);

        boolean existsByCategoryIdAndUserId(Long categoryId, Long userId);

        @Query("""
                        SELECT COALESCE(SUM(t.amount),0)
                        FROM Transaction t
                        WHERE t.user.id = :userId
                        AND t.type = 'INCOME'
                        """)
        BigDecimal getTotalIncome(Long userId);

        @Query("""
                        SELECT COALESCE(SUM(t.amount),0)
                        FROM Transaction t
                        WHERE t.user.id = :userId
                        AND t.type = 'EXPENSE'
                        """)
        BigDecimal getTotalExpense(Long userId);

        // Category breakdown
        @Query("""
                        SELECT t.category.id,
                               t.category.name,
                               SUM(t.amount)
                        FROM Transaction t
                        WHERE t.user.id = :userId
                        AND t.type = 'EXPENSE'
                        GROUP BY t.category.id, t.category.name
                        ORDER BY SUM(t.amount) DESC
                        """)
        List<Object[]> getCategoryExpenseBreakdown(Long userId);

        // Monthly trend 
        @Query("""
                        SELECT YEAR(t.transactionDate),
                               MONTH(t.transactionDate),
                               SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END),
                               SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END)
                        FROM Transaction t
                        WHERE t.user.id = :userId
                        GROUP BY YEAR(t.transactionDate), MONTH(t.transactionDate)
                        ORDER BY YEAR(t.transactionDate), MONTH(t.transactionDate)
                        """)
        List<Object[]> getMonthlyTrend(Long userId);

        //Total Expense By Category And Month
        @Query("""
                        SELECT COALESCE(SUM(t.amount), 0)
                        FROM Transaction t
                        WHERE t.user.id = :userId
                        AND t.category.id = :categoryId
                        AND t.type = 'EXPENSE'
                        AND YEAR(t.transactionDate) = :year
                        AND MONTH(t.transactionDate) = :month
                        """)
        java.math.BigDecimal getTotalExpenseByCategoryAndMonth(Long userId, Long categoryId, Integer month,
                        Integer year);
}
