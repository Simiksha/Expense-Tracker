package com.smartspend.expensetracker.service.alert;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

import com.smartspend.expensetracker.enums.BudgetStatus;
import com.smartspend.expensetracker.model.Budget;
import com.smartspend.expensetracker.repository.BudgetRepository;
import com.smartspend.expensetracker.repository.TransactionRepository;
import com.smartspend.expensetracker.service.auth.EmailService;
import com.smartspend.expensetracker.service.notification.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BudgetAlertServiceImpl implements BudgetAlertService {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Override
    public void checkAndCreateAlert(Long userId, Long categoryId, Integer month, Integer year) {

        Budget budget = budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(
                userId, categoryId, month, year).orElse(null);

        if (budget == null) {
            return;
        }

        BigDecimal spentAmount = transactionRepository.getTotalExpenseByCategoryAndMonth(
                userId, categoryId, month, year);

        BigDecimal usagePercentage = BigDecimal.ZERO;
        if (budget.getLimitAmount().compareTo(BigDecimal.ZERO) > 0) {
            usagePercentage = spentAmount
                    .multiply(BigDecimal.valueOf(100))
                    .divide(budget.getLimitAmount(), 2, RoundingMode.HALF_UP);
        }

        BudgetStatus status = getBudgetStatus(usagePercentage);

        if (status == BudgetStatus.NEARING_LIMIT) {
            String title = "Budget nearing limit";
            String message = "Your budget for category '" + budget.getCategory().getName() +
                    "' is at " + usagePercentage + "% for " + month + "/" + year;

            String referenceKey = buildReferenceKey(categoryId, month, year, BudgetStatus.NEARING_LIMIT);

            boolean created = notificationService.createNotification(
                    userId,
                    title,
                    message,
                    "BUDGET_ALERT",
                    referenceKey);

            if (created) {
                emailService.sendEmail(
                        budget.getUser().getEmail(),
                        title,
                        message);
            }

        } else if (status == BudgetStatus.EXCEEDED) {
            String title = "Budget exceeded";
            String message = "Your budget for category '" + budget.getCategory().getName() +
                    "' has exceeded the limit. Usage: " + usagePercentage + "% for " + month + "/" + year;

            String referenceKey = buildReferenceKey(categoryId, month, year, BudgetStatus.EXCEEDED);

            boolean created = notificationService.createNotification(
                    userId,
                    title,
                    message,
                    "BUDGET_ALERT",
                    referenceKey);

            if (created) {
                emailService.sendEmail(
                        budget.getUser().getEmail(),
                        title,
                        message);
            }
        }
    }

    private String buildReferenceKey(Long categoryId, Integer month, Integer year, BudgetStatus status) {
        return "BUDGET_" + categoryId + "_" + month + "_" + year + "_" + status.name();
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