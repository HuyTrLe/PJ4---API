package com.mytech.api.services.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mytech.api.models.InsufficientFundsException;
import com.mytech.api.models.budget.ParamBudget;
import com.mytech.api.models.category.CateTypeENum;
import com.mytech.api.models.category.Category;
import com.mytech.api.models.expense.Expense;
import com.mytech.api.models.income.Income;
import com.mytech.api.models.saving_goals.SavingGoal;
import com.mytech.api.models.saving_goals.TransactionWithSaving;
import com.mytech.api.models.transaction.FindTransactionParam;
import com.mytech.api.models.transaction.Transaction;
import com.mytech.api.models.transaction.TransactionDTO;
import com.mytech.api.models.transaction.TransactionData;
import com.mytech.api.models.transaction.TransactionReport;
import com.mytech.api.models.transaction.TransactionView;
import com.mytech.api.models.wallet.Wallet;
import com.mytech.api.repositories.categories.CategoryRepository;
import com.mytech.api.repositories.expense.ExpenseRepository;
import com.mytech.api.repositories.income.IncomeRepository;
import com.mytech.api.repositories.saving_goals.Saving_goalsRepository;
import com.mytech.api.repositories.transaction.TransactionRepository;
import com.mytech.api.repositories.wallet.WalletRepository;
import com.mytech.api.services.budget.BudgetService;
import com.mytech.api.services.category.CategoryService;
import com.mytech.api.services.wallet.WalletService;

import jakarta.persistence.EntityNotFoundException;

@Service
public class TransactionServiceImpl implements TransactionService {

	private final TransactionRepository transactionRepository;
	private final BudgetService budgetService;
	private final ModelMapper modelMapper;
	private final WalletService walletService;
	private final CategoryRepository categoryRepository;
	private final WalletRepository walletRepository;
	private final IncomeRepository incomeRepository;
	private final ExpenseRepository expenseRepository;
	private final Saving_goalsRepository saving_goalsRepository;
	private final CategoryService categoryService;

	public TransactionServiceImpl(TransactionRepository transactionRepository, BudgetService budgetService,
			WalletService walletService, ModelMapper modelMapper, CategoryRepository categoryRepository,
			WalletRepository walletRepository, IncomeRepository incomeRepository, ExpenseRepository expenseRepository,
			Saving_goalsRepository saving_goalsRepository, CategoryService categoryService) {
		this.transactionRepository = transactionRepository;
		this.budgetService = budgetService;
		this.walletService = walletService;
		this.modelMapper = modelMapper;
		this.categoryRepository = categoryRepository;
		this.walletRepository = walletRepository;
		this.incomeRepository = incomeRepository;
		this.expenseRepository = expenseRepository;
		this.saving_goalsRepository = saving_goalsRepository;
		this.categoryService = categoryService;
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
		LocalDate oldTransactionDate = null;
		Wallet wallet = transaction.getWallet();

		if (isUpdate) {
			// If updating, retrieve the old transaction before making changes
			Transaction oldTransaction = transactionRepository.findById(transaction.getTransactionId()).orElseThrow(
					() -> new RuntimeException("Transaction not found with id: " + transaction.getTransactionId()));
			oldAmount = oldTransaction.getAmount();
			oldTransactionDate = oldTransaction.getTransactionDate();
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
		// if (potentialNewBalance.compareTo(BigDecimal.ZERO) < 0) {
		// throw new InsufficientFundsException("Insufficient funds in wallet after
		// transaction.");
		// }

		if (wallet.getWalletType() == 3) {
			adjustGoalsAndSaveTransaction(transaction);
		}

		// Since there are sufficient funds, update the wallet balance
		wallet.setBalance(potentialNewBalance);
		walletRepository.save(wallet);

		// Now save the transaction
		Transaction savedTransaction = transactionRepository.save(transaction);

		// Adjust the budget if necessary
		if (isUpdate) {
			budgetService.adjustBudgetForTransaction(savedTransaction, true, oldAmount, oldTransactionDate);
		} else {
			budgetService.adjustBudgetForTransaction(savedTransaction, false, BigDecimal.ZERO,
					transaction.getTransactionDate());
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
		BigDecimal transactionAmount = transaction.getAmount();
		LocalDate transactionDate = transaction.getTransactionDate();
		boolean isDeletion = true;

		budgetService.adjustBudgetForTransaction(transaction, isDeletion, transactionAmount, transactionDate);

		if (transaction.getWallet().getWalletType() == 3) {
			adjustGoalsAndDeleteTransaction(transaction);
		}

		transactionRepository.delete(transaction);
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

		if (existingTransaction.getNotes() != null && existingTransaction.getNotes().contains("Transfer Money")) {
			throw new IllegalArgumentException("Transaction Transfer Money cannot be updated");
		}

		BigDecimal oldAmount = existingTransaction.getAmount();
		Category oldCategory = existingTransaction.getCategory();
		LocalDate oldTransactionDate = existingTransaction.getTransactionDate();
		boolean categoryChanged = transactionDTO.getCategoryId() != null
				&& !transactionDTO.getCategoryId().equals(oldCategory.getId());
		boolean dateChanged = !oldTransactionDate.equals(transactionDTO.getTransactionDate());

		// Fetch wallet and get current balance
		Wallet wallet = existingTransaction.getWallet();
		BigDecimal walletBalance = wallet.getBalance();

		if (wallet.getWalletType() == 3) {
			adjustGoalsAndUpdateTransaction(transactionId, transactionDTO);
		}

		// Revert the original transaction from the balance
		BigDecimal correctedBalance = walletBalance;
		if (oldCategory.getType() == CateTypeENum.INCOME) {
			correctedBalance = correctedBalance.subtract(oldAmount);
		} else if (oldCategory.getType() == CateTypeENum.EXPENSE) {
			correctedBalance = correctedBalance.add(oldAmount);
		}

		// Update the category if changed
		Category newCategory = oldCategory;
		if (categoryChanged) {
			newCategory = categoryRepository.findById(transactionDTO.getCategoryId()).orElseThrow(
					() -> new RuntimeException("Category not found with id: " + transactionDTO.getCategoryId()));
		}

		// Adjust the balance with the updated transaction
		if (newCategory.getType() == CateTypeENum.INCOME) {
			walletBalance = correctedBalance.add(transactionDTO.getAmount());
		} else if (newCategory.getType() == CateTypeENum.EXPENSE) {
			walletBalance = correctedBalance.subtract(transactionDTO.getAmount());
		}

		// Check for insufficient funds
		// if (walletBalance.compareTo(BigDecimal.ZERO) < 0) {
		// throw new InsufficientFundsException("Not enough balance in the wallet for
		// the updated transaction.");
		// }

		wallet.setBalance(walletBalance); // Update wallet with new calculated balance
		walletRepository.save(wallet);

		existingTransaction.setCategory(newCategory);
		existingTransaction.setTransactionDate(transactionDTO.getTransactionDate());
		existingTransaction.setNotes(transactionDTO.getNotes());
		existingTransaction.setAmount(transactionDTO.getAmount());

		// Adjust budget only if category or amount has changed
		if (categoryChanged || !oldAmount.equals(transactionDTO.getAmount())) {
			budgetService.adjustBudgetForCategory(oldCategory.getId(), oldAmount.negate());
			if (categoryChanged) {
				budgetService.adjustBudgetForCategory(transactionDTO.getCategoryId(), transactionDTO.getAmount());
				System.out.println("transactiondto amount: " + transactionDTO.getAmount());
			} else {
				budgetService.adjustBudgetForCategory(oldCategory.getId(), transactionDTO.getAmount());
			}
		}

		if (dateChanged) {
			budgetService.adjustBudgetForTransaction(existingTransaction, false, oldAmount, oldTransactionDate);
		}

		// Handle Income/Expense Record Update
		if (oldCategory.getType() == CateTypeENum.EXPENSE) {
			Expense expense = expenseRepository.findByTransaction(existingTransaction);
			if (expense != null) {
				if (categoryChanged && existingTransaction.getCategory().getType() == CateTypeENum.INCOME) {
					existingTransaction.setExpense(null);

					// Create a new Income record since the new category is INCOME
					Income newIncome = new Income();
					newIncome.setTransaction(existingTransaction); // Link the transaction
					newIncome.setAmount(transactionDTO.getAmount()); // Use updated amount
					newIncome.setIncomeDate(transactionDTO.getTransactionDate()); // Use updated transaction date
					newIncome.setCategory(existingTransaction.getCategory()); // Use the new (updated) category
					newIncome.setWallet(existingTransaction.getWallet()); // Use associated wallet
					newIncome.setUser(existingTransaction.getUser()); // Use associated user
					incomeRepository.save(newIncome); // Persist the new Income entity
				} else {
					// Update the existing Expense record if there's no category change
					expense.setAmount(transactionDTO.getAmount()); // Update the amount
					expenseRepository.save(expense); // Persist the updates
				}
			}
		} else if (oldCategory.getType() == CateTypeENum.INCOME) {
			Income income = incomeRepository.findByTransaction(existingTransaction);
			if (income != null) {
				if (!"USD".equals(existingTransaction.getWallet().getCurrency())) {
					if (categoryChanged && existingTransaction.getCategory().getType() == CateTypeENum.EXPENSE) {
						// Tạo một khoản chi mới
						existingTransaction.setIncome(null);

						Expense newExpense = new Expense();
						newExpense.setTransaction(existingTransaction); // Liên kết với giao dịch
						newExpense.setAmount(transactionDTO.getAmount()); // Sử dụng số tiền đã cập nhật
						newExpense.setExpenseDate(transactionDTO.getTransactionDate()); // Sử dụng ngày giao dịch đã cập
																						// nhật
						newExpense.setCategory(existingTransaction.getCategory()); // Sử dụng danh mục mới (đã cập nhật)
						newExpense.setWallet(existingTransaction.getWallet()); // Sử dụng ví liên kết
						newExpense.setUser(existingTransaction.getUser()); // Sử dụng người dùng liên kết
						expenseRepository.save(newExpense); // Lưu thực thể khoản chi mới
					} else {
						// Update bản ghi thu nhập hiện có nếu không có thay đổi danh mục
						income.setAmount(transactionDTO.getAmount()); // Cập nhật số tiền
						incomeRepository.save(income); // Lưu các cập nhật
					}
				} else {
					throw new IllegalArgumentException("Expense transaction not allowed for USD wallet");
				}
			}
		}

		transactionRepository.save(existingTransaction);

		// budgetService.adjustBudgetForTransaction(existingTransaction, false,
		// oldAmount, oldTransactionDate);
		System.out.println("transactiondto amount: " + transactionDTO.getAmount());

		return modelMapper.map(existingTransaction, TransactionDTO.class);
	}

	@Override
	public List<TransactionView> getTop5NewTransaction(int userId) {
		PageRequest pageable = PageRequest.of(0, 5);
		Page<TransactionView> transactionsPage = transactionRepository.getTop5NewTransaction(userId, pageable);
		List<TransactionView> transactions = transactionsPage.getContent();
		return transactions;
	}

	@Override
	public List<TransactionView> getTop5TransactionHightestMoney(ParamBudget param) {
		Pageable pageable = PageRequest.of(0, 5);
		Page<TransactionView> transactionsPage = transactionRepository.getTop5TransactionHightestMoney(
				param.getUserId(), param.getFromDate(), param.getToDate(),
				pageable);
		List<TransactionView> transactions = transactionsPage.getContent();
		return transactions;
	}

	@Override
	public List<TransactionData> getTransactionWithTime(ParamBudget param) {
		// TODO Auto-generated method stub
		return transactionRepository.getTransactionWithTime(param.getUserId(), param.getFromDate(), param.getToDate());
	}

	@Override
	public List<TransactionReport> getTransactionReport(ParamBudget param) {
		// TODO Auto-generated method stub
		return transactionRepository.getTransactionReport(param.getUserId(), param.getFromDate(), param.getToDate());
	}

	@Override
	public List<TransactionReport> getTransactionReportMonth(ParamBudget param) {
		LocalDate previousMonthStart = param.getFromDate().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth()); // April
																														// 1,
																														// 2024
		LocalDate previousMonthEnd = param.getFromDate().minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()); // April
																													// 30,
																													// 2024
		return transactionRepository.getTransactionReportMonth(param.getUserId(), param.getFromDate(),
				param.getToDate(), previousMonthStart, previousMonthEnd);
	}

	@Override
	public List<TransactionData> FindTransaction(FindTransactionParam param) {
		var result = CateTypeENum.valueOf(param.getType().toUpperCase());
		return transactionRepository.FindTransaction(param.getUserId(), param.getFromDate(), param.getToDate(), result,param.getWalletId());
	}

	public List<TransactionView> getTop5NewTransactionforWallet(int userId, Integer walletId) {
		PageRequest pageable = PageRequest.of(0, 5);
		Page<TransactionView> transactionsPage = transactionRepository.getTop5NewTransactionforWallet(userId, walletId,
				pageable);
		List<TransactionView> transactions = transactionsPage.getContent();
		return transactions;
	}

	private void adjustGoalsAndSaveTransaction(Transaction transaction) {

		Wallet existingWallet = walletService.getWalletById(transaction.getWallet().getWalletId());
		Category existingCategory = categoryService.getByCateId(transaction.getCategory().getId());
		SavingGoal selectedSavingGoal = saving_goalsRepository.findById(transaction.getSavingGoal().getId())
				.orElseThrow(() -> new RuntimeException(
						"Saving goal not found with id: " + transaction.getSavingGoal().getId()));
		if ((selectedSavingGoal.getEndDate() != null && LocalDate.now().isAfter(selectedSavingGoal.getEndDate()))) {
			throw new IllegalArgumentException(
					"This saving goal has either reached its is past its end date.");
		}
		BigDecimal walletBalance = existingWallet.getBalance();
		BigDecimal transactionAmount = transaction.getAmount(); // Extracting transaction amount

		if (existingCategory.getType() == CateTypeENum.INCOME) {
			walletBalance = existingWallet.getBalance().add(transactionAmount);
			selectedSavingGoal.setCurrentAmount(selectedSavingGoal.getCurrentAmount().add(transactionAmount));
		} else if (existingCategory.getType() == CateTypeENum.EXPENSE) {
			walletBalance = existingWallet.getBalance().subtract(transactionAmount);
			selectedSavingGoal.setCurrentAmount(selectedSavingGoal.getCurrentAmount().subtract(transactionAmount));
		}
		// Lưu lại mục tiêu tiết kiệm đã được cập nhật
		saving_goalsRepository.save(selectedSavingGoal);
	}

	@Transactional
	private void adjustGoalsAndUpdateTransaction(Integer transactionId, TransactionDTO transactionDTO) {
		Transaction existingTransaction = transactionRepository.findById(transactionId)
				.orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + transactionId));
		Wallet existingWallet = walletService.getWalletById(transactionDTO.getWalletId());
		Category existingCategory = categoryService.getByCateId(transactionDTO.getCategoryId());
		SavingGoal selectedSavingGoal = saving_goalsRepository.findById(transactionDTO.getSavingGoalId())
				.orElseThrow(() -> new EntityNotFoundException(
						"Saving goal not found with id: " + transactionDTO.getSavingGoalId()));
		if ((selectedSavingGoal.getEndDate() != null && LocalDate.now().isAfter(selectedSavingGoal.getEndDate()))) {
			throw new IllegalArgumentException(
					"This saving goal has either reached its target amount or is past its end date.");
		}
		BigDecimal oldAmount = existingTransaction.getAmount();
		BigDecimal newAmount = transactionDTO.getAmount();
		BigDecimal difference = newAmount.subtract(oldAmount);
		BigDecimal walletBalanceUpdate = existingCategory.getType() == CateTypeENum.INCOME ? difference
				: difference.negate();

		BigDecimal updatedWalletBalance = existingWallet.getBalance().add(walletBalanceUpdate);

		existingWallet.setBalance(updatedWalletBalance);
		selectedSavingGoal.setCurrentAmount(selectedSavingGoal.getCurrentAmount().add(walletBalanceUpdate));
		saving_goalsRepository.save(selectedSavingGoal);
		walletRepository.save(existingWallet); // Assuming there's a method to save wallet updates
	}

	private void adjustGoalsAndDeleteTransaction(Transaction transaction) {
		Wallet existingWallet = walletService.getWalletById(transaction.getWallet().getWalletId());
		Category existingCategory = categoryService.getByCateId(transaction.getCategory().getId());
		SavingGoal selectedSavingGoal = saving_goalsRepository.findById(transaction.getSavingGoal().getId())
				.orElseThrow(() -> new EntityNotFoundException(
						"Saving goal not found with id: " + transaction.getSavingGoal().getId()));

		BigDecimal transactionAmount = transaction.getAmount();
		// Adjust balances based on the type of transaction
		if (existingCategory.getType() == CateTypeENum.INCOME) {
			// Reverse the income effect: subtract from wallet and goal
			selectedSavingGoal.setCurrentAmount(selectedSavingGoal.getCurrentAmount().subtract(transactionAmount));
		} else if (existingCategory.getType() == CateTypeENum.EXPENSE) {
			// Reverse the expense effect: add back to wallet and goal
			selectedSavingGoal.setCurrentAmount(selectedSavingGoal.getCurrentAmount().add(transactionAmount));
		}

		// Save the updated wallet and saving goal information
		walletRepository.save(existingWallet);
		saving_goalsRepository.save(selectedSavingGoal);

	}

	@Override
	public List<TransactionData> getTransactionWithBudget(ParamBudget param) {
		return transactionRepository.getTransactionWithBudget(param.getUserId(), param.getCategoryId(),
				param.getFromDate(), param.getToDate());
	}

	@Override
	public List<TransactionData> getTransactionWithSaving(TransactionWithSaving param) {
		
		return transactionRepository.getTransactionWithSaving(param.getUserId(), param.getGoalId());
	}

}
