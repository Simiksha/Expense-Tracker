package com.smartspend.expensetracker.service.transaction;

import com.smartspend.expensetracker.dto.transaction.TransactionFilterRequest;
import com.smartspend.expensetracker.dto.transaction.TransactionRequest;
import com.smartspend.expensetracker.dto.transaction.TransactionResponse;
import com.smartspend.expensetracker.model.Category;
import com.smartspend.expensetracker.model.Transaction;
import com.smartspend.expensetracker.model.User;
import com.smartspend.expensetracker.enums.TransactionType;
import com.smartspend.expensetracker.exception.BadRequestException;
import com.smartspend.expensetracker.exception.ResourceNotFoundException;
import com.smartspend.expensetracker.repository.CategoryRepository;
import com.smartspend.expensetracker.repository.TransactionRepository;
import com.smartspend.expensetracker.repository.specification.TransactionSpecification;
import com.smartspend.expensetracker.service.alert.BudgetAlertService;
import com.smartspend.expensetracker.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "transactionDate", "amount", "title", "createdAt"
    );

    private static final Set<String> ALLOWED_SORT_DIRECTIONS = Set.of("asc", "desc");

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;
    private final BudgetAlertService budgetAlertService;

    @Override
    public TransactionResponse createTransaction(TransactionRequest request) {
        User currentUser = userService.getCurrentUser();

        Category category = categoryRepository.findByIdAndUserId(request.categoryId(), currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Transaction transaction = Transaction.builder()
                .title(request.title().trim())
                .amount(request.amount())
                .type(request.type())
                .transactionDate(request.transactionDate())
                .description(request.description())
                .category(category)
                .user(currentUser)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        if (savedTransaction.getType() == TransactionType.EXPENSE) {
            budgetAlertService.checkAndCreateAlert(
                    currentUser.getId(),
                    savedTransaction.getCategory().getId(),
                    savedTransaction.getTransactionDate().getMonthValue(),
                    savedTransaction.getTransactionDate().getYear()
            );
        }

        return mapToResponse(savedTransaction);
    }

    @Override
    public List<TransactionResponse> getMyTransactions(TransactionFilterRequest filter) {
        User currentUser = userService.getCurrentUser();

        validateFilter(filter);

        String sortBy = (filter.sortBy() == null || filter.sortBy().isBlank())
                ? "transactionDate"
                : filter.sortBy();

        String sortDir = (filter.sortDir() == null || filter.sortDir().isBlank())
                ? "desc"
                : filter.sortDir().toLowerCase();

        Sort sort = sortDir.equals("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        return transactionRepository.findAll(
                        TransactionSpecification.filterBy(currentUser.getId(), filter),
                        sort
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public TransactionResponse getMyTransactionById(Long id) {
        User currentUser = userService.getCurrentUser();

        Transaction transaction = transactionRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        return mapToResponse(transaction);
    }

    @Override
    public TransactionResponse updateTransaction(Long id, TransactionRequest request) {
        User currentUser = userService.getCurrentUser();

        Transaction transaction = transactionRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        Long oldCategoryId = transaction.getCategory().getId();
        int oldMonth = transaction.getTransactionDate().getMonthValue();
        int oldYear = transaction.getTransactionDate().getYear();
        TransactionType oldType = transaction.getType();

        Category category = categoryRepository.findByIdAndUserId(request.categoryId(), currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        transaction.setTitle(request.title().trim());
        transaction.setAmount(request.amount());
        transaction.setType(request.type());
        transaction.setTransactionDate(request.transactionDate());
        transaction.setDescription(request.description());
        transaction.setCategory(category);

        Transaction savedTransaction = transactionRepository.save(transaction);

        if (oldType == TransactionType.EXPENSE) {
            budgetAlertService.checkAndCreateAlert(
                    currentUser.getId(),
                    oldCategoryId,
                    oldMonth,
                    oldYear
            );
        }

        if (savedTransaction.getType() == TransactionType.EXPENSE) {
            budgetAlertService.checkAndCreateAlert(
                    currentUser.getId(),
                    savedTransaction.getCategory().getId(),
                    savedTransaction.getTransactionDate().getMonthValue(),
                    savedTransaction.getTransactionDate().getYear()
            );
        }

        return mapToResponse(savedTransaction);
    }

    @Override
    public void deleteTransaction(Long id) {
        User currentUser = userService.getCurrentUser();

        Transaction transaction = transactionRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        Long categoryId = transaction.getCategory().getId();
        int month = transaction.getTransactionDate().getMonthValue();
        int year = transaction.getTransactionDate().getYear();
        TransactionType type = transaction.getType();

        transactionRepository.delete(transaction);

        if (type == TransactionType.EXPENSE) {
            budgetAlertService.checkAndCreateAlert(
                    currentUser.getId(),
                    categoryId,
                    month,
                    year
            );
        }
    }

    private void validateFilter(TransactionFilterRequest filter) {
        if (filter.startDate() != null && filter.endDate() != null
                && filter.startDate().isAfter(filter.endDate())) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        if (filter.minAmount() != null && filter.maxAmount() != null
                && filter.minAmount().compareTo(filter.maxAmount()) > 0) {
            throw new BadRequestException("Minimum amount cannot be greater than maximum amount");
        }

        if (filter.sortBy() != null && !filter.sortBy().isBlank()
                && !ALLOWED_SORT_FIELDS.contains(filter.sortBy())) {
            throw new BadRequestException("Invalid sortBy value");
        }

        if (filter.sortDir() != null && !filter.sortDir().isBlank()
                && !ALLOWED_SORT_DIRECTIONS.contains(filter.sortDir().toLowerCase())) {
            throw new BadRequestException("Invalid sortDir value");
        }
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getTitle(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getTransactionDate(),
                transaction.getDescription(),
                transaction.getCategory().getId(),
                transaction.getCategory().getName()
        );
    }
}