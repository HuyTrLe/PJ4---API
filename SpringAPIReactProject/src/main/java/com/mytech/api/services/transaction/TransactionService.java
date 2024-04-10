package com.mytech.api.services.transaction;

import java.util.List;

import com.mytech.api.models.transaction.Transaction;

public interface TransactionService {
	Transaction saveTransaction(Transaction transaction);

    Transaction getTransactionById(Integer transactionId);

    List<Transaction> getAllTransactionsByUserId(Integer userId);

    void deleteTransaction(Integer transactionId);
}
