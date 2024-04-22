package com.mytech.api.services.transaction;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mytech.api.models.category.CateTypeENum;
import com.mytech.api.models.transaction.Transaction;
import com.mytech.api.repositories.transaction.TransactionRepository;

@Service
public class TransactionServiceImpl implements TransactionService {

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
	public Page<Transaction> getAllTransactionsByUserId(Integer userId, Pageable pageable) {
		return transactionRepository.findByUserId(userId, pageable);
	}

	@Override
	public List<Transaction> getAllTransactionsByAllWallet(int userId) {
		return transactionRepository.getByUserId(userId);
	}

	@Override
	public void deleteTransaction(Integer transactionId) {
		Transaction transaction = getTransactionById(transactionId);
		transactionRepository.delete(transaction);
	}

	@Override
	public BigDecimal getTotalIncomeByUserId(int userId) {
		return transactionRepository.getTotalAmountByUserIdAndCategoryType(userId, CateTypeENum.INCOME) != null
				? transactionRepository.getTotalAmountByUserIdAndCategoryType(userId, CateTypeENum.INCOME)
				: BigDecimal.ZERO;
	}

	@Override
	public BigDecimal getTotalExpenseByUserId(int userId) {
		return transactionRepository.getTotalExpenseByUserId(userId, CateTypeENum.EXPENSE) != null
				? transactionRepository.getTotalExpenseByUserId(userId, CateTypeENum.EXPENSE)
				: BigDecimal.ZERO;
	}

	@Override
	public BigDecimal getTotalIncomeByWalletId(int userId, int walletId) {
		return transactionRepository.getTotalAmountByUserIdAndWalletId(userId, walletId, CateTypeENum.INCOME) != null
				? transactionRepository.getTotalAmountByUserIdAndWalletId(userId, walletId, CateTypeENum.INCOME)
				: BigDecimal.ZERO;
	}

	@Override
	public BigDecimal getTotalExpenseByWalletId(int userId, int walletId) {
		return transactionRepository.getTotalAmountByUserIdAndWalletId(userId, walletId, CateTypeENum.EXPENSE) != null
				? transactionRepository.getTotalAmountByUserIdAndWalletId(userId, walletId, CateTypeENum.EXPENSE)
				: BigDecimal.ZERO;
	}

	@Override
	public List<Transaction> getTransactionsByWalletId(int userId, Integer walletId) {
		return transactionRepository.findByUserIdAndWallet_WalletId(userId, walletId);
	}

}
