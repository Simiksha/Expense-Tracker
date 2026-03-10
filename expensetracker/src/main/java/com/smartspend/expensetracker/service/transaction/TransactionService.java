package com.smartspend.expensetracker.service.transaction;

import java.util.List;

import com.smartspend.expensetracker.dto.transaction.TransactionFilterRequest;
import com.smartspend.expensetracker.dto.transaction.TransactionRequest;
import com.smartspend.expensetracker.dto.transaction.TransactionResponse;

public interface TransactionService {
    TransactionResponse createTransaction(TransactionRequest request);
    List<TransactionResponse> getMyTransactions(TransactionFilterRequest filter);
    TransactionResponse getMyTransactionById(Long id);
    TransactionResponse updateTransaction(Long id, TransactionRequest request);
    void deleteTransaction(Long id);
}
