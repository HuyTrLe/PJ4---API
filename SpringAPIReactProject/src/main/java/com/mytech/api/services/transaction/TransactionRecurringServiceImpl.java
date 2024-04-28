package com.mytech.api.services.transaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mytech.api.models.transaction.TransactionRecurring;
import com.mytech.api.repositories.transaction.TransactionRecurringRepository;

@Service
public class TransactionRecurringServiceImpl implements TransactionRecurringService {

    @Autowired
    TransactionRecurringRepository transactionRecurringRepository;

    @Override
    public TransactionRecurring saveTransactionsRecurring(TransactionRecurring transactionRecurring) {
        return transactionRecurringRepository.save(transactionRecurring);
    }

    @Override
    public TransactionRecurring getTransactionsRecurringById(Integer transactionId) {
        return transactionRecurringRepository.findById(transactionId).orElse(null);
    }

    @Override
    public Page<TransactionRecurring> getAllTransactionsRecurringByUserId(Integer userId, Pageable pageable) {
        return transactionRecurringRepository.findByUserId(userId, pageable);
    }

    @Override
    public void deleteTransactionRecurring(Integer transactionId) {
        TransactionRecurring transactionRecurring = getTransactionsRecurringById(transactionId);
        transactionRecurringRepository.delete(transactionRecurring);
    }

}
