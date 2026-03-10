package com.smartspend.expensetracker.repository.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.smartspend.expensetracker.dto.transaction.TransactionFilterRequest;
import com.smartspend.expensetracker.model.Transaction;

import jakarta.persistence.criteria.Predicate;

public class TransactionSpecification {
    public static Specification<Transaction> filterBy(Long userId, TransactionFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("user").get("id"), userId));

            if (filter.keyword() != null && !filter.keyword().isBlank()) {
                String pattern = "%" + filter.keyword().trim().toLowerCase() + "%";
                predicates.add(
                        cb.or(
                                cb.like(cb.lower(root.get("title")), pattern),
                                cb.like(cb.lower(root.get("description")), pattern),
                                cb.like(cb.lower(root.get("category").get("name")), pattern)
                        )
                );
            }

            if (filter.type() != null) {
                predicates.add(cb.equal(root.get("type"), filter.type()));
            }

            if (filter.categoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filter.categoryId()));
            }

            if (filter.startDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), filter.startDate()));
            }

            if (filter.endDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), filter.endDate()));
            }

            if (filter.minAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), filter.minAmount()));
            }

            if (filter.maxAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), filter.maxAmount()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
