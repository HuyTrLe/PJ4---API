package com.mytech.api.services.transaction;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mytech.api.models.category.CateTypeENum;
import com.mytech.api.models.transaction.Transaction;
import com.mytech.api.repositories.transaction.TransactionRepository;
import com.mytech.api.services.budget.BudgetService;

@Service
public class TransactionServiceImpl implements TransactionService {

	private final TransactionRepository transactionRepository;
	private final BudgetService budgetService;

	public TransactionServiceImpl(TransactionRepository transactionRepository, BudgetService budgetService) {
		this.transactionRepository = transactionRepository;
		this.budgetService = budgetService;
	}

	@Override
	public Transaction saveTransaction(Transaction transaction) {
		boolean isUpdate = transaction.getTransactionId() != null;
		Transaction oldTransaction = null;

		if (isUpdate) {
			// If updating, retrieve the old transaction before making changes
			oldTransaction = transactionRepository.findById(transaction.getTransactionId())
					.orElseThrow(() -> new RuntimeException(
							"Transaction not found with id: " + transaction.getTransactionId()));
		}

		// Save the transaction
		Transaction savedTransaction = transactionRepository.save(transaction);

		// Adjust the budget
		budgetService.adjustBudgetForTransaction(savedTransaction, false, oldTransaction);

		return savedTransaction;
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
		if (transaction != null) {
			transactionRepository.delete(transaction);
			// Pass the old transaction (before deletion) and indicate it's a deletion
			budgetService.adjustBudgetForTransaction(transaction, true, transaction);
		}
	}

	@Override
	public List<Transaction> getIncomeByUserIdAndCategoryType(int userId, Enum type) {
		return transactionRepository.getIncomeByUserIdAndCategoryType(userId, CateTypeENum.INCOME) != null
				? transactionRepository.getIncomeByUserIdAndCategoryType(userId, CateTypeENum.INCOME)
				: null;
	}

	@Override
	public List<Transaction> getExpenseByUserIdAndCategoryType(int userId, Enum type) {
		return transactionRepository.getExpenseByUserIdAndCategoryType(userId, CateTypeENum.EXPENSE) != null
				? transactionRepository.getExpenseByUserIdAndCategoryType(userId, CateTypeENum.EXPENSE)
				: null;
	}

	@Override
	public List<Transaction> getTotalIncomeByWalletId(int userId, int walletId, Enum type) {
		return transactionRepository.getTotalAmountByUserIdAndWallet_WalletIdAndCategoryType(userId, walletId,
				CateTypeENum.INCOME) != null
						? transactionRepository.getTotalAmountByUserIdAndWallet_WalletIdAndCategoryType(userId,
								walletId, CateTypeENum.INCOME)
						: null;
	}

	@Override
	public List<Transaction> getTotalExpenseByWalletId(int userId, int walletId, Enum type) {
		return transactionRepository.getTotalExpenseByUserIdAndWallet_WalletIdAndCategoryType(userId, walletId,
				CateTypeENum.EXPENSE) != null
						? transactionRepository.getTotalExpenseByUserIdAndWallet_WalletIdAndCategoryType(
								userId, walletId, CateTypeENum.EXPENSE)
						: null;
	}

	@Override
	public List<Transaction> getTransactionsByWalletId(int userId, Integer walletId) {
		return transactionRepository.findByUserIdAndWallet_WalletId(userId, walletId);
	}

}
