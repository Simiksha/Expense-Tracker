package com.smartspend.expensetracker.service.alert;

public interface BudgetAlertService {
    
    void checkAndCreateAlert(Long userId, Long categoryId, Integer month, Integer year);
    
}
