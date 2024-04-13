package com.mytech.api.services.transaction;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mytech.api.models.transaction.Transaction;
import com.mytech.api.repositories.transaction.TransactionRepository;

@Service
public class TransactionServiceImpl implements TransactionService{

	private final TransactionRepository transactionRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Transaction saveTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    @Override
    public Transaction getTransactionById(Integer transactionId) {
        return transactionRepository.findById(transactionId).orElse(null);
    }

    @Override
    public Page<Transaction>  getAllTransactionsByUserId(Integer userId, Pageable pageable) {
    	 return transactionRepository.findByUserId(userId, pageable);
    }

    @Override
    public void deleteTransaction(Integer transactionId) {
        Transaction transaction = getTransactionById(transactionId);
        transactionRepository.delete(transaction);
    }

}
