package com.mytech.api.services.transaction;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mytech.api.models.budget.ParamBudget;
import com.mytech.api.models.transaction.FindTransactionParam;
import com.mytech.api.models.transaction.Transaction;
import com.mytech.api.models.transaction.TransactionDTO;
import com.mytech.api.models.transaction.TransactionData;
import com.mytech.api.models.transaction.TransactionReport;
import com.mytech.api.models.transaction.TransactionView;

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

    List<TransactionView> getTop5NewTransaction(int userId);

    List<TransactionView> getTop5TransactionHightestMoney(ParamBudget param);
   
    List<TransactionData> getTransactionWithTime(ParamBudget param);
    
    List<TransactionReport> getTransactionReport(ParamBudget param);
    List<TransactionReport> getTransactionReportMonth(ParamBudget param);
    
    List<TransactionData> FindTransaction(FindTransactionParam param);
}
