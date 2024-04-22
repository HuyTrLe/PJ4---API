package com.mytech.api.services.transaction;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mytech.api.models.transaction.Transaction;

public interface TransactionService {
    Transaction saveTransaction(Transaction transaction);

    Transaction getTransactionById(Integer transactionId);

    List<Transaction> getAllTransactionsByAllWallet(int userId);

    Page<Transaction> getAllTransactionsByUserId(Integer userId, Pageable pageable);

    void deleteTransaction(Integer transactionId);

    BigDecimal getTotalIncomeByUserId(int userId);

    BigDecimal getTotalExpenseByUserId(int userId);

    BigDecimal getTotalIncomeByWalletId(int userId, int walletId);

    BigDecimal getTotalExpenseByWalletId(int userId, int walletId);

    List<Transaction> getTransactionsByWalletId(int userId, Integer walletId);
}
