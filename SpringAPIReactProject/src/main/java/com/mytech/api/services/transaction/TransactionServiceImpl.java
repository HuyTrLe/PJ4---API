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

import com.mytech.api.auth.repositories.UserRepository;
import com.mytech.api.models.InsufficientFundsException;
import com.mytech.api.models.budget.ParamBudget;
import com.mytech.api.models.category.CateTypeENum;
import com.mytech.api.models.category.Category;
import com.mytech.api.models.expense.Expense;
import com.mytech.api.models.income.Income;
import com.mytech.api.models.saving_goals.EndDateType;
import com.mytech.api.models.saving_goals.SavingGoal;
import com.mytech.api.models.transaction.FindTransactionParam;
import com.mytech.api.models.transaction.Transaction;
import com.mytech.api.models.transaction.TransactionDTO;
import com.mytech.api.models.transaction.TransactionData;
import com.mytech.api.models.transaction.TransactionReport;
import com.mytech.api.models.transaction.TransactionSavingGoalsView;
import com.mytech.api.models.transaction.TransactionView;
import com.mytech.api.models.user.User;
import com.mytech.api.models.wallet.Wallet;
import com.mytech.api.repositories.categories.CategoryRepository;
import com.mytech.api.repositories.expense.ExpenseRepository;
import com.mytech.api.repositories.income.IncomeRepository;
import com.mytech.api.repositories.saving_goals.Saving_goalsRepository;
import com.mytech.api.repositories.transaction.TransactionRepository;
import com.mytech.api.repositories.wallet.WalletRepository;
import com.mytech.api.services.budget.BudgetService;
import com.mytech.api.services.category.CategoryService;
import com.mytech.api.services.saving_goals.SavingGoalsService;
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
	private final SavingGoalsService savingGoalsService;
	private final CategoryService categoryService;
	private final UserRepository userRepository;

	public TransactionServiceImpl(TransactionRepository transactionRepository, BudgetService budgetService,
			WalletService walletService, ModelMapper modelMapper, CategoryRepository categoryRepository,
			WalletRepository walletRepository, IncomeRepository incomeRepository, ExpenseRepository expenseRepository,
			Saving_goalsRepository saving_goalsRepository, CategoryService categoryService,
			UserRepository userRepository, SavingGoalsService savingGoalsService) {
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
		this.userRepository = userRepository;
		this.savingGoalsService = savingGoalsService;
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
	public TransactionDTO createTransaction(TransactionDTO transactionDTO) {
		Transaction transaction = modelMapper.map(transactionDTO, Transaction.class);
		User user = userRepository.findById(transactionDTO.getUserId())
				.orElseThrow(
						() -> new IllegalArgumentException("User not found with id: " + transactionDTO.getUserId()));

		Wallet wallet = walletRepository.findById(transactionDTO.getWalletId())
				.orElseThrow(() -> new IllegalArgumentException(
						"Wallet not found with id: " + transactionDTO.getWalletId()));

		Category category = categoryService.getByCateId(transactionDTO.getCategoryId());
		if (category == null) {
			new IllegalArgumentException("Category not found with id: " + transactionDTO.getCategoryId());
		}

		transaction.setUser(user);
		transaction.setWallet(wallet);
		transaction.setCategory(category);
		transaction.setAmount(transaction.getAmount());
		transaction.setTransactionDate(transaction.getTransactionDate());

		switch (category.getType()) {
			case INCOME:
				Income income = new Income();
				income.setUser(user);
				income.setWallet(wallet);
				income.setAmount(transaction.getAmount());
				income.setIncomeDate(transaction.getTransactionDate());
				income.setCategory(category);
				income.setTransaction(transaction);
				transaction.setIncome(income);
				break;
			case EXPENSE:
				if (!"USD".equals(wallet.getCurrency())) {
					Expense expense = new Expense();
					expense.setUser(user);
					expense.setWallet(wallet);
					expense.setAmount(transaction.getAmount());
					expense.setExpenseDate(transaction.getTransactionDate());
					expense.setCategory(category);
					expense.setTransaction(transaction);
					transaction.setExpense(expense);
				} else {
					throw new IllegalArgumentException("Expense transaction not allowed for USD wallet");
				}
				break;
			default:
				break;
		}

		BigDecimal potentialNewBalance = wallet.getBalance();
		if (transaction.getIncome() != null) {
			potentialNewBalance = potentialNewBalance.add(transaction.getAmount());
		} else if (transaction.getExpense() != null) {
			potentialNewBalance = potentialNewBalance.subtract(transaction.getAmount());
		}

		if (wallet.getWalletType() == 3) {
			List<SavingGoal> goals = saving_goalsRepository.findByWallet_WalletId(transactionDTO.getWalletId());
			if (!goals.isEmpty()) {
				boolean hasActiveGoal = goals.stream()
						.anyMatch(goal -> (goal.getStartDate().isBefore(LocalDate.now())
								|| goal.getStartDate().isEqual(LocalDate.now())) // Start date là trước đó hoặc bằng
																					// ngày
																					// hiện tại
								&& (goal.getEndDate() == null || goal.getEndDate().isAfter(LocalDate.now()))); // End
																												// date
																												// là
																												// null
																												// (forever)
																												// hoặc
																												// là
																												// sau
																												// ngày
																												// hiện
																												// tại
				if (!hasActiveGoal) {
					throw new IllegalArgumentException("A saving goal must be selected for goal-type wallets");
				}

				if (hasActiveGoal) {
					LocalDate transactionDate = transactionDTO.getTransactionDate();
					Long savingGoalId = transactionDTO.getSavingGoalId();

					if (transactionDate == null) {
						throw new IllegalArgumentException("Transaction date cannot be null");
					}

					if (savingGoalId == null || savingGoalId == 0) {
						throw new IllegalArgumentException("A saving goal must be selected for goal-type wallets");
					}

					SavingGoal selectedSavingGoal = saving_goalsRepository.findById(savingGoalId)
							.orElseThrow(() -> new RuntimeException("Invalid saving goal ID: " + savingGoalId));

					if (selectedSavingGoal.getEndDateType() == EndDateType.END_DATE) {
						LocalDate startDate = selectedSavingGoal.getStartDate();
						LocalDate endDate = selectedSavingGoal.getEndDate();

						if (transactionDate.isBefore(startDate) || transactionDate.isAfter(endDate)) {
							throw new IllegalArgumentException(
									"Transaction date must be within the goal's start and end dates");
						}
					}

					selectedSavingGoal.setCurrentAmount(potentialNewBalance);
					saving_goalsRepository.save(selectedSavingGoal);
					transaction.setSavingGoal(selectedSavingGoal);
				}
			} else {
				throw new IllegalArgumentException(
						"A saving goal must be create to selected for goal-type wallets");
			}

		}

		wallet.setBalance(potentialNewBalance);
		walletRepository.save(wallet);

		Transaction createdTransaction = transactionRepository.save(transaction);
		if (createdTransaction.getSavingGoal() != null) {
	        savingGoalsService.checkAndSendSavingGoalProgressNotifications(createdTransaction.getSavingGoal());
	    }
		return modelMapper.map(createdTransaction, TransactionDTO.class);
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

		wallet.setBalance(walletBalance); // Update wallet with new calculated balance
		walletRepository.save(wallet);

		existingTransaction.setCategory(newCategory);
		existingTransaction.setTransactionDate(transactionDTO.getTransactionDate());
		existingTransaction.setNotes(transactionDTO.getNotes());
		existingTransaction.setAmount(transactionDTO.getAmount());

		Long savingGoalId = transactionDTO.getSavingGoalId();
		if (savingGoalId != null && savingGoalId != 0) {
			List<SavingGoal> goals = saving_goalsRepository.findByWallet_WalletId(transactionDTO.getWalletId());
			LocalDate transactionDate = transactionDTO.getTransactionDate();

			if (transactionDate == null) {
				throw new IllegalArgumentException("Transaction date cannot be null");
			}

			SavingGoal selectedSavingGoal = goals.stream()
					.filter(g -> g.getId().equals(savingGoalId))
					.findFirst()
					.orElseThrow(() -> new RuntimeException("Invalid saving goal ID: " + savingGoalId));

			if (transactionDate.isBefore(selectedSavingGoal.getStartDate()) ||
					(selectedSavingGoal.getEndDate() != null
							&& transactionDate.isAfter(selectedSavingGoal.getEndDate()))) {
				throw new IllegalArgumentException("Transaction date must be within the saving goal's duration");
			}
			selectedSavingGoal.setCurrentAmount(walletBalance);
			existingTransaction.setSavingGoal(selectedSavingGoal);
		}

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
			}
		}

		transactionRepository.save(existingTransaction);

		// budgetService.adjustBudgetForTransaction(existingTransaction, false,
		// oldAmount, oldTransactionDate);
		if (existingTransaction.getSavingGoal() != null) {
	        savingGoalsService.checkAndSendSavingGoalProgressNotifications(existingTransaction.getSavingGoal());
	    }
		System.out.println("transactiondto amount: " + transactionDTO.getAmount());

		return modelMapper.map(existingTransaction, TransactionDTO.class);
	}

	@Override
	public List<TransactionSavingGoalsView> getBySavingGoal_IdAndUser_Id(Long savingGoalId, Long userId) {
		return transactionRepository.getBySavingGoal_IdAndUser_Id(savingGoalId, userId) != null
				? transactionRepository.getBySavingGoal_IdAndUser_Id(savingGoalId, userId)
				: null;
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
		return transactionRepository.FindTransaction(param.getUserId(), param.getFromDate(), param.getToDate(), result,
				param.getWalletId());
	}

	public List<TransactionView> getTop5NewTransactionforWallet(int userId, Integer walletId) {
		PageRequest pageable = PageRequest.of(0, 5);
		Page<TransactionView> transactionsPage = transactionRepository.getTop5NewTransactionforWallet(userId, walletId,
				pageable);
		List<TransactionView> transactions = transactionsPage.getContent();
		return transactions;
	}

	private void adjustGoalsAndDeleteTransaction(Transaction transaction) {
		// Check if the transaction is associated with a saving goal
		if (transaction.getSavingGoal() != null) {
			// Retrieve the saving goal associated with the transaction
			SavingGoal selectedSavingGoal = saving_goalsRepository.findById(transaction.getSavingGoal().getId())
					.orElseThrow(() -> new EntityNotFoundException(
							"Saving goal not found with id: " + transaction.getSavingGoal().getId()));
			BigDecimal transactionAmount = transaction.getAmount();
			Category existingCategory = transaction.getCategory();
			if (existingCategory.getType() == CateTypeENum.INCOME) {
				// Reverse the income effect: subtract from wallet and goal
				selectedSavingGoal.setCurrentAmount(selectedSavingGoal.getCurrentAmount().subtract(transactionAmount));
			} else if (existingCategory.getType() == CateTypeENum.EXPENSE) {
				// Reverse the expense effect: add back to wallet and goal
				selectedSavingGoal.setCurrentAmount(selectedSavingGoal.getCurrentAmount().add(transactionAmount));
			}
			saving_goalsRepository.save(selectedSavingGoal);
		}
	}

	@Override
	public List<TransactionData> getTransactionWithBudget(ParamBudget param) {
		return transactionRepository.getTransactionWithBudget(param.getUserId(), param.getCategoryId(),
				param.getFromDate(), param.getToDate());
	}

}
