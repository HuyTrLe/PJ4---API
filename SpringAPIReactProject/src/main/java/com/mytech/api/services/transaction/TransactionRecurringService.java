package com.mytech.api.services.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mytech.api.models.transaction.TransactionRecurring;

public interface TransactionRecurringService {
    TransactionRecurring saveTransactionsRecurring(TransactionRecurring transactionRecurring);

    TransactionRecurring getTransactionsRecurringById(Integer transactionRecurringId);

    Page<TransactionRecurring> getAllTransactionsRecurringByUserId(Integer userId, Pageable pageable);

    void deleteTransactionRecurring(Integer transactionRecurringId);
}
