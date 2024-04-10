package com.mytech.api.services.transaction;

import java.util.List;

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
    public List<Transaction> getAllTransactionsByUserId(Integer userId) {
        return transactionRepository.findByUserId(userId);
    }

    @Override
    public void deleteTransaction(Integer transactionId) {
        Transaction transaction = getTransactionById(transactionId);
        transactionRepository.delete(transaction);
    }

}
