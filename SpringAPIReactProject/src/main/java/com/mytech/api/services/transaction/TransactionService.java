package com.mytech.api.services.transaction;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mytech.api.models.transaction.Transaction;
import com.mytech.api.models.transaction.TransactionDTO;

public interface TransactionService {
    Transaction saveTransaction(Transaction transaction);

    Transaction getTransactionById(Integer transactionId);
    
    TransactionDTO updateTransaction(Integer transactionId, TransactionDTO transactionDTO);

    List<Transaction> getAllTransactionsByAllWallet(int userId);

    Page<Transaction> getAllTransactionsByUserId(Integer userId, Pageable pageable);

    void deleteTransaction(Integer transactionId);

    List<Transaction> getIncomeByUserIdAndCategoryType(int userId, Enum type);

    List<Transaction> getExpenseByUserIdAndCategoryType(int userId, Enum type);

    List<Transaction> getTotalIncomeByWalletId(int userId, int walletId, Enum type);

    List<Transaction> getTotalExpenseByWalletId(int userId, int walletId, Enum type);

    List<Transaction> getTransactionsByWalletId(int userId, Integer walletId);
}
