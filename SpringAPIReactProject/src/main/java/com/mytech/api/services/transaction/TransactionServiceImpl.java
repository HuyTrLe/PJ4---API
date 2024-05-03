package com.mytech.api.services.transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mytech.api.models.InsufficientFundsException;
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

	private void adjustWalletBalance(Transaction transaction) {
		Wallet wallet = transaction.getWallet();
		BigDecimal newBalance = wallet.getBalance();
		if (transaction.getIncome() != null) {
			newBalance = newBalance.add(transaction.getAmount());
		} else if (transaction.getExpense() != null) {
			newBalance = newBalance.subtract(transaction.getAmount());
		}
		if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
			throw new InsufficientFundsException("Insufficient funds in wallet after transaction.");
		}
		wallet.setBalance(newBalance);
		walletRepository.save(wallet);
	}

	private void revertWalletBalanceIfNeeded(Transaction oldTransaction) {
		Wallet wallet = oldTransaction.getWallet();
		BigDecimal balanceAdjustment = oldTransaction.getAmount();

		// Determine if the transaction was an income or expense to adjust the balance
		// correctly
		if (oldTransaction.getIncome() != null) {
			// If it was income, subtract it from the wallet balance to revert its effect
			wallet.setBalance(wallet.getBalance().subtract(balanceAdjustment));
		} else if (oldTransaction.getExpense() != null) {
			// If it was an expense, add it back to the wallet balance to revert its effect
			wallet.setBalance(wallet.getBalance().add(balanceAdjustment));
		}

		// Check if the balance adjustment results in a negative balance, which should
		// not happen
		if (wallet.getBalance().compareTo(BigDecimal.ZERO) < 0) {
			throw new RuntimeException("Reverting the transaction results in a negative wallet balance.");
		}

		// Save the updated wallet balance
		walletRepository.save(wallet);
	}

	@Override
	public Transaction saveTransaction(Transaction transaction) {
		boolean isUpdate = transaction.getTransactionId() != null && transaction.getTransactionId() != 0;
		BigDecimal oldAmount = null;
		Wallet wallet = transaction.getWallet();

		if (isUpdate) {
			// If updating, retrieve the old transaction before making changes
			Transaction oldTransaction = transactionRepository.findById(transaction.getTransactionId()).orElseThrow(
					() -> new RuntimeException("Transaction not found with id: " + transaction.getTransactionId()));
			oldAmount = oldTransaction.getAmount();
			// Revert the old transaction's effect on the wallet balance
			revertWalletBalanceIfNeeded(oldTransaction);
		}

		// Perform a balance check before saving the transaction
		BigDecimal potentialNewBalance = wallet.getBalance();
		if (transaction.getIncome() != null) {
			potentialNewBalance = potentialNewBalance.add(transaction.getAmount());
		} else if (transaction.getExpense() != null) {
			potentialNewBalance = potentialNewBalance.subtract(transaction.getAmount());
		}
		if (potentialNewBalance.compareTo(BigDecimal.ZERO) < 0) {
			throw new InsufficientFundsException("Insufficient funds in wallet after transaction.");
		}

		// Since there are sufficient funds, update the wallet balance
		wallet.setBalance(potentialNewBalance);
		walletRepository.save(wallet);

		// Now save the transaction
		Transaction savedTransaction = transactionRepository.save(transaction);

		// Adjust the budget if necessary
		if (isUpdate) {
			budgetService.adjustBudgetForTransaction(savedTransaction, true, oldAmount);
		} else {
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
			budgetService.adjustBudgetForTransaction(transaction, true, transaction.getAmount());
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
	@Transactional(rollbackFor = InsufficientFundsException.class)
	public TransactionDTO updateTransaction(Integer transactionId, TransactionDTO transactionDTO) {
		Transaction existingTransaction = transactionRepository.findById(transactionId)
				.orElseThrow(() -> new RuntimeException("Transaction not found with id: " + transactionId));

		BigDecimal oldAmount = existingTransaction.getAmount();
		Category oldCategory = existingTransaction.getCategory();
		Integer currentWalletId = existingTransaction.getWallet().getWalletId();
		Integer newWalletId = transactionDTO.getWalletId();

		try {
			// Revert the old transaction's effect on the wallet balance before updating
			revertWalletBalanceIfNeeded(existingTransaction);

			// Check if the category has changed
			boolean categoryChanged = transactionDTO.getCategoryId() != null
					&& !transactionDTO.getCategoryId().equals(oldCategory.getId());

			// Update the transaction fields
			existingTransaction.setAmount(transactionDTO.getAmount());
			existingTransaction.setTransactionDate(transactionDTO.getTransactionDate());
			existingTransaction.setNotes(transactionDTO.getNotes());

			// Update the category if changed
			Category newCategory = null;
			if (categoryChanged) {
				newCategory = categoryRepository.findById(transactionDTO.getCategoryId()).orElseThrow(
						() -> new RuntimeException("Category not found with id: " + transactionDTO.getCategoryId()));
				existingTransaction.setCategory(newCategory);
			}

			// Update the wallet if changed
			if (newWalletId != null && !newWalletId.equals(currentWalletId)) {
				Wallet newWallet = walletRepository.findById(transactionDTO.getWalletId()).orElseThrow(
						() -> new RuntimeException("Wallet not found with id: " + transactionDTO.getWalletId()));
				existingTransaction.setWallet(newWallet);
			}

			// Save the updated transaction
			Transaction updatedTransaction = transactionRepository.save(existingTransaction);

			// Adjust the wallet balance with the new transaction amount
			adjustWalletBalance(updatedTransaction);

			// Adjust the budget for the old category by subtracting the old amount
			Optional<Budget> oldCategoryBudget = budgetService.findBudgetByCategoryId(oldCategory.getId());
			if (categoryChanged && oldCategoryBudget.isPresent()) {
			    budgetService.adjustBudgetForCategory(oldCategory.getId(), oldAmount.negate());
			}

			// Adjust the budget for the new category by adding the new amount
			if (categoryChanged) {
			    Optional<Budget> newCategoryBudget = budgetService.findBudgetByCategoryId(newCategory.getId());
			    if (newCategoryBudget.isPresent()) {
			        budgetService.adjustBudgetForCategory(newCategory.getId(), transactionDTO.getAmount());
			    }
			    // If there's no budget for the new category, skip the adjustment
			} else if (!oldAmount.equals(transactionDTO.getAmount())) {
			    // If the category hasn't changed, but the transaction amount has, adjust the
			    // budget accordingly
			    budgetService.adjustBudgetForCategory(oldCategory.getId(), transactionDTO.getAmount().subtract(oldAmount));
			}

			// Return the updated transaction as DTO
			return modelMapper.map(updatedTransaction, TransactionDTO.class);
		} catch (InsufficientFundsException e) {
			throw e;
		}
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
