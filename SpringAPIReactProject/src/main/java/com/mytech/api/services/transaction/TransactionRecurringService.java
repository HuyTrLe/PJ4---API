package com.mytech.api.services.transaction;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.mytech.api.models.bill.Bill;
import com.mytech.api.models.debt.ReportDebtParam;
import com.mytech.api.models.transaction.TransactionRecurring;
import com.mytech.api.models.transaction.TransactionRecurringDTO;

public interface TransactionRecurringService {
    TransactionRecurring getTransactionsRecurringById(Integer transactionRecurringId);

    Page<TransactionRecurring> getAllTransactionsRecurringByUserId(Integer userId, Pageable pageable);

    ResponseEntity<?> deleteTransaction(Integer transactionId, Authentication authentication);

    ResponseEntity<?> createTransaction(TransactionRecurringDTO transactionRecurringDTO);

    ResponseEntity<?> updateTransaction(Integer transactionId, TransactionRecurringDTO transactionRecurringDTO);

    List<TransactionRecurring> findByRecurrence_DueDate(LocalDate dueDate);
    
    List<TransactionRecurring> findRecuActive(ReportDebtParam param);

	List<TransactionRecurring> findRecuExpired(ReportDebtParam param);
}
