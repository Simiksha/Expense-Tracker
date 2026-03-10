package com.smartspend.expensetracker.service.dashboard;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.smartspend.expensetracker.dto.dashboard.CategoryBreakdownResponse;
import com.smartspend.expensetracker.dto.dashboard.DashboardSummaryResponse;
import com.smartspend.expensetracker.dto.dashboard.MonthlyTrendResponse;
import com.smartspend.expensetracker.model.User;
import com.smartspend.expensetracker.repository.TransactionRepository;
import com.smartspend.expensetracker.service.user.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private final TransactionRepository transactionRepository;
    private final UserService userService;

    @Override
    public DashboardSummaryResponse getSummary() {

        User currentUser = userService.getCurrentUser();

        BigDecimal income = transactionRepository.getTotalIncome(currentUser.getId());
        BigDecimal expense = transactionRepository.getTotalExpense(currentUser.getId());

        BigDecimal balance = income.subtract(expense);

        return new DashboardSummaryResponse(income, expense, balance);
    }

    @Override
    public List<CategoryBreakdownResponse> getCategoryBreakdown() {

        User currentUser = userService.getCurrentUser();

        return transactionRepository.getCategoryExpenseBreakdown(currentUser.getId())
                .stream()
                .map(row -> new CategoryBreakdownResponse(
                        (Long) row[0],
                        (String) row[1],
                        (BigDecimal) row[2]
                ))
                .toList();
    }

    @Override
    public List<MonthlyTrendResponse> getMonthlyTrend() {

        User currentUser = userService.getCurrentUser();

        return transactionRepository.getMonthlyTrend(currentUser.getId())
                .stream()
                .map(row -> new MonthlyTrendResponse(
                        (Integer) row[0],
                        (Integer) row[1],
                        (BigDecimal) row[2],
                        (BigDecimal) row[3]
                ))
                .toList();
    }
}
