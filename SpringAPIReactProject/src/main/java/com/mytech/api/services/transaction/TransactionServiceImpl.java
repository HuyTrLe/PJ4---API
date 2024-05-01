package com.mytech.api.services.transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mytech.api.models.budget.Budget;
import com.mytech.api.models.category.CateTypeENum;
import com.mytech.api.models.category.Category;
import com.mytech.api.models.transaction.Transaction;
import com.mytech.api.models.transaction.TransactionDTO;
import com.mytech.api.models.transaction.TransactionView;
import com.mytech.api.models.wallet.Wallet;
import com.mytech.api.repositories.categories.CategoryRepository;
import com.mytech.api.repositories.transaction.TransactionRepository;
import com.mytech.api.repositories.wallet.WalletRepository;
import com.mytech.api.services.budget.BudgetService;
import com.mytech.api.services.wallet.WalletService;

@Service
public class TransactionServiceImpl implements TransactionService {

	private final TransactionRepository transactionRepository;
	private final BudgetService budgetService;
	private final ModelMapper modelMapper;
	private final WalletService walletService;
	private final CategoryRepository categoryRepository;
	private final WalletRepository walletRepository;

	public TransactionServiceImpl(TransactionRepository transactionRepository, BudgetService budgetService,
			WalletService walletService, ModelMapper modelMapper, CategoryRepository categoryRepository,
			WalletRepository walletRepository) {
		this.transactionRepository = transactionRepository;
		this.budgetService = budgetService;
		this.walletService = walletService;
		this.modelMapper = modelMapper;
		this.categoryRepository = categoryRepository;
		this.walletRepository = walletRepository;
	}

	@Override
	public Transaction saveTransaction(Transaction transaction) {
		boolean isUpdate = transaction.getTransactionId() != null;
		BigDecimal oldAmount = null;

		if (isUpdate) {
			// If updating, retrieve the old transaction before making changes
			Transaction oldTransaction = transactionRepository.findById(transaction.getTransactionId()).orElseThrow(
					() -> new RuntimeException("Transaction not found with id: " + transaction.getTransactionId()));
			oldAmount = oldTransaction.getAmount();
		}

		// Save the transaction
		Transaction savedTransaction = transactionRepository.save(transaction);

		// Adjust the budget
		if (isUpdate) {
			// If updating, pass the old amount to adjustBudgetForTransaction
			budgetService.adjustBudgetForTransaction(savedTransaction, false, oldAmount);
		} else {
			// If creating, pass zero as the old amount
			budgetService.adjustBudgetForTransaction(savedTransaction, false, BigDecimal.ZERO);
		}

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
			// Before deleting the transaction, adjust the budget to account for its
			// removal.
			// Since the transaction is being deleted, the old amount is the transaction's
			// amount.
			budgetService.adjustBudgetForTransaction(transaction, true, transaction.getAmount());

			// Now, delete the transaction from the repository.
			transactionRepository.delete(transaction);
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

	@Override
	public TransactionDTO updateTransaction(Integer transactionId, TransactionDTO transactionDTO) {
		Transaction existingTransaction = transactionRepository.findById(transactionId)
				.orElseThrow(() -> new RuntimeException("Transaction not found with id: " + transactionId));

		BigDecimal oldAmount = existingTransaction.getAmount();
		Category oldCategory = existingTransaction.getCategory();

		// Check if the category has changed
		boolean categoryChanged = transactionDTO.getCategoryId() != null &&
				!transactionDTO.getCategoryId().equals(oldCategory.getId());

		// Update the transaction fields
		existingTransaction.setAmount(transactionDTO.getAmount());
		existingTransaction.setTransactionDate(transactionDTO.getTransactionDate());
		existingTransaction.setNotes(transactionDTO.getNotes());

		// Update the category if changed
		if (categoryChanged) {
			Category newCategory = categoryRepository.findById(transactionDTO.getCategoryId())
					.orElseThrow(() -> new RuntimeException(
							"Category not found with id: " + transactionDTO.getCategoryId()));
			existingTransaction.setCategory(newCategory);

			Optional<Budget> oldCategoryBudget = budgetService.findBudgetByCategoryId(oldCategory.getId());
			if (oldCategoryBudget.isPresent()) {
				// Adjust the budget for the old category
				budgetService.adjustBudgetForCategory(oldCategory.getId(), oldAmount.negate());
			}

			// Adjust the budget for the new category
			// Check if a budget exists for the new category
			Optional<Budget> newCategoryBudget = budgetService.findBudgetByCategoryId(newCategory.getId());
			if (newCategoryBudget.isPresent()) {
				budgetService.adjustBudgetForCategory(newCategory.getId(), transactionDTO.getAmount());
			}
		}

		// Update the wallet if changed
		if (transactionDTO.getWalletId() != 0) {
			Wallet newWallet = walletRepository.findById(transactionDTO.getWalletId())
					.orElseThrow(
							() -> new RuntimeException("Wallet not found with id: " + transactionDTO.getWalletId()));
			existingTransaction.setWallet(newWallet);
		}

		// Calculate the amount change for the wallet balance
		BigDecimal amountChange = transactionDTO.getAmount().subtract(oldAmount);
		adjustWalletBalance(existingTransaction, amountChange);

		// Save the updated transaction
		Transaction updatedTransaction = transactionRepository.save(existingTransaction);
		if (!categoryChanged) {
			budgetService.adjustBudgetForTransaction(updatedTransaction, false, oldAmount);
		}

		// Return the updated transaction as DTO
		return modelMapper.map(updatedTransaction, TransactionDTO.class);
	}

	private void adjustWalletBalance(Transaction transaction, BigDecimal amountChange) {
		Wallet wallet = transaction.getWallet();
		BigDecimal newBalance = wallet.getBalance().add(amountChange);
		if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
			throw new RuntimeException("Insufficient funds in the wallet for transaction update.");
		}
		wallet.setBalance(newBalance);
		walletService.saveWallet(wallet);
	}

	@Override
	public List<TransactionView> getTop5NewTransaction(int userId) {
		PageRequest pageable = PageRequest.of(0, 5);
		Page<TransactionView> transactionsPage = transactionRepository.getTop5NewTransaction(userId, pageable);
		List<TransactionView> transactions = transactionsPage.getContent();
		return transactions;
	}

	@Override
	public List<TransactionView> getTop5TransactionHightestMoney(int userId) {
		Pageable pageable = PageRequest.of(0, 5);
		Page<TransactionView> transactionsPage = transactionRepository.getTop5TransactionHightestMoney(userId,
				pageable);
		List<TransactionView> transactions = transactionsPage.getContent();
		return transactions;
	}

}
