package com.mytech.api.services.wallet;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.mytech.api.auth.repositories.UserRepository;
import com.mytech.api.models.category.Category;
import com.mytech.api.models.expense.Expense;
import com.mytech.api.models.income.Income;
import com.mytech.api.models.saving_goals.SavingGoal;
import com.mytech.api.models.transaction.Transaction;
import com.mytech.api.models.wallet.TransferRequest;
import com.mytech.api.models.wallet.Wallet;
import com.mytech.api.models.wallet.WalletDTO;
import com.mytech.api.repositories.categories.CategoryRepository;
import com.mytech.api.repositories.expense.ExpenseRepository;
import com.mytech.api.repositories.income.IncomeRepository;
import com.mytech.api.repositories.saving_goals.Saving_goalsRepository;
import com.mytech.api.repositories.transaction.TransactionRepository;
import com.mytech.api.repositories.wallet.WalletRepository;

@Service
public class WalletServiceImpl implements WalletService {

	private final WalletRepository walletRepository;
	private final TransactionRepository transactionRepository;
	private final CategoryRepository categoryRepository;
	private final IncomeRepository incomeRepository;
	private final ExpenseRepository expenseRepository;
	private final Saving_goalsRepository saving_goalsRepository;
	private final ModelMapper modelMapper;

	public WalletServiceImpl(WalletRepository walletRepository, UserRepository userRepository,
			TransactionRepository transactionRepository, CategoryRepository categoryRepository,
			IncomeRepository incomeRepository, ExpenseRepository expenseRepository,
			Saving_goalsRepository saving_goalsRepository, ModelMapper modelMapper) {
		this.walletRepository = walletRepository;
		this.transactionRepository = transactionRepository;
		this.categoryRepository = categoryRepository;
		this.incomeRepository = incomeRepository;
		this.expenseRepository = expenseRepository;
		this.saving_goalsRepository = saving_goalsRepository;
		this.modelMapper = modelMapper;
	}

	@Override
	public WalletDTO createWallet(WalletDTO walletDTO) {
		Wallet wallet = modelMapper.map(walletDTO, Wallet.class);

		if (walletRepository.existsByWalletNameAndUserId(wallet.getWalletName(), wallet.getUser().getId())) {
			throw new IllegalArgumentException("Wallet name already exists");
		}

		if (wallet.getWalletType() == 3 && wallet.getBalance().compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("Wallet Goals cannot have a negative balance");
		}

		if (wallet.getCurrency().equals("USD") && walletRepository.existsByCurrencyAndUserId(wallet.getCurrency(), wallet.getUser().getId())) {
		    throw new IllegalArgumentException("Only one wallet allowed per currency (USD) for each user");
		}

		String currency = wallet.getCurrency();
		if (!isValidCurrency(currency)) {
			throw new IllegalArgumentException("Invalid currency");
		}

//		if (currency.equals("USD") && walletRepository.existsByCurrency(currency)) {
//			throw new IllegalArgumentException("Only one wallet allowed per currency (USD)");
//		}

		BigDecimal newBalance = wallet.getBalance().add(wallet.getBalance());
		wallet.setBalance(newBalance);
		walletRepository.save(wallet);
		Transaction transaction = new Transaction();
		transaction.setWallet(wallet);
		transaction.setTransactionDate(LocalDate.now());
		transaction.setAmount(wallet.getBalance().abs());
		transaction.setUser(wallet.getUser());

		 if (currency.equals("USD")) {
		        List<Category> incomeCategories = categoryRepository.findByNameAndUserId("Incoming Transfer", wallet.getUser().getId());
		        if (!incomeCategories.isEmpty()) {
		            Category incomeCategory = incomeCategories.get(0);
		            transaction.setCategory(incomeCategory);
		            transaction = transactionRepository.save(transaction);

		            Income income = new Income();
		            income.setAmount(wallet.getBalance().abs());
		            income.setIncomeDate(LocalDate.now());
		            income.setUser(wallet.getUser());
		            income.setTransaction(transaction);
		            income.setWallet(wallet);
		            income.setCategory(incomeCategory);
		            incomeRepository.save(income);
		        }
		    } else if (currency.equals("VND")) {
		        if (wallet.getBalance().compareTo(BigDecimal.ZERO) > 0) {
		            List<Category> incomeCategories = categoryRepository.findByNameAndUserId("Incoming Transfer", wallet.getUser().getId());
		            if (!incomeCategories.isEmpty()) {
		                Category incomeCategory = incomeCategories.get(0);
		                createIncomeTransaction(wallet, newBalance, transaction, incomeCategory);
		            }
		        } else {
		            List<Category> expenseCategories = categoryRepository.findByNameAndUserId("Outgoing Transfer", wallet.getUser().getId());
		            if (!expenseCategories.isEmpty()) {
		                Category expenseCategory = expenseCategories.get(0);
		                createExpenseTransaction(wallet, newBalance, transaction, expenseCategory);
		            }
		        }
		    }

		Wallet createdWallet = walletRepository.save(wallet);
		return modelMapper.map(createdWallet, WalletDTO.class);
	}

	@Override
	public WalletDTO updateWallet(int walletId, WalletDTO walletDTO) {
		Wallet existingWallet = walletRepository.findById(walletId)
				.orElseThrow(() -> new RuntimeException("Wallet not found with id: " + walletId));

		if (walletDTO == null) {
			return modelMapper.map(existingWallet, WalletDTO.class);
		}

		if (existingWallet.getWalletType() == 3 && walletDTO.getBalance().compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("Wallet Goals cannot have a negative balance");
		}

		if ("USD".equals(existingWallet.getCurrency()) && walletDTO.getBalance().compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("Wallet USD cannot have a negative balance");
		}

		if (!existingWallet.getWalletName().equals(walletDTO.getWalletName()) &&
				walletRepository.existsByWalletNameAndWalletIdNot(walletDTO.getWalletName(), walletId)) {
			throw new IllegalArgumentException("Wallet name already exists");
		}

		BigDecimal oldBalance = existingWallet.getBalance();
		// Update balance and save the wallet
		existingWallet.setWalletName(walletDTO.getWalletName());
		existingWallet.setBalance(walletDTO.getBalance());
		existingWallet = walletRepository.save(existingWallet);

		// Calculate balance difference
		BigDecimal balanceDifference = existingWallet.getBalance().subtract(oldBalance);

		// If balance difference is not zero, handle accordingly
		if (balanceDifference.compareTo(BigDecimal.ZERO) != 0) {
			if (existingWallet.getWalletType() == 3) {
				// Fetch associated goals
				List<SavingGoal> goals = saving_goalsRepository.findByWallet_WalletId(walletId);
				if (!goals.isEmpty()) {
					// If there are goals, a valid goal ID must be selected
					Long savingGoalId = walletDTO.getSavingGoalId();
					if (savingGoalId == null || savingGoalId == 0) {
						throw new IllegalArgumentException("A saving goal must be selected for goal-type wallets");
					}
					SavingGoal selectedSavingGoal = goals.stream()
							.filter(g -> g.getId().equals(savingGoalId))
							.findFirst()
							.orElseThrow(() -> new RuntimeException("Invalid saving goal ID: " + savingGoalId));
					BigDecimal currentAmount = selectedSavingGoal.getCurrentAmount() != null
							? selectedSavingGoal.getCurrentAmount()
							: BigDecimal.ZERO;
					BigDecimal newGoalBalance = currentAmount.add(balanceDifference);
					selectedSavingGoal.setCurrentAmount(newGoalBalance);
					saving_goalsRepository.save(selectedSavingGoal);

					// Create transaction for the goal adjustment
					Transaction goalTransaction = new Transaction();
					goalTransaction.setWallet(existingWallet);
					goalTransaction.setTransactionDate(LocalDate.now());
					goalTransaction.setAmount(balanceDifference.abs());
					goalTransaction.setUser(existingWallet.getUser());
					goalTransaction.setSavingGoal(selectedSavingGoal);
					// Determine category based on balance difference
					Category category;
					if (balanceDifference.compareTo(BigDecimal.ZERO) > 0) {
					    List<Category> incomeCategories = categoryRepository.findByNameAndUserId("Incoming Transfer", existingWallet.getUser().getId());
					    category = !incomeCategories.isEmpty() ? incomeCategories.get(0) : null;
					} else {
					    List<Category> expenseCategories = categoryRepository.findByNameAndUserId("Outgoing Transfer", existingWallet.getUser().getId());
					    category = !expenseCategories.isEmpty() ? expenseCategories.get(0) : null;
					}

					// Set category for the transaction
					if (category != null) {
					    goalTransaction.setCategory(category);
					    transactionRepository.save(goalTransaction);
					}
				}
			} else {
				// Create transaction for the balance adjustment
				Transaction balanceTransaction = new Transaction();
				balanceTransaction.setWallet(existingWallet);
				balanceTransaction.setTransactionDate(LocalDate.now());
				balanceTransaction.setAmount(balanceDifference.abs());
				balanceTransaction.setUser(existingWallet.getUser());

				// Determine category based on balance difference
				Category category;
				if (balanceDifference.compareTo(BigDecimal.ZERO) > 0) {
				    List<Category> incomeCategories = categoryRepository.findByNameAndUserId("Incoming Transfer", existingWallet.getUser().getId());
				    category = !incomeCategories.isEmpty() ? incomeCategories.get(0) : null;
				} else {
				    List<Category> expenseCategories = categoryRepository.findByNameAndUserId("Outgoing Transfer", existingWallet.getUser().getId());
				    category = !expenseCategories.isEmpty() ? expenseCategories.get(0) : null;
				}

				// Set category for the transaction
				if (category != null) {
				    balanceTransaction.setCategory(category);
				    transactionRepository.save(balanceTransaction);
				}
			}
		}

		return modelMapper.map(existingWallet, WalletDTO.class);
	}

	private boolean isValidCurrency(String currency) {
		return currency.equals("VND") || currency.equals("USD");
	}

	@Override
	public void transferUSDToVND(TransferRequest transferRequest) {
		Wallet sourceWallet = walletRepository.findById(transferRequest.getSourceWalletId())
				.orElseThrow(() -> new RuntimeException(
						"Source wallet not found with id: " + transferRequest.getSourceWalletId()));

		Wallet destinationWallet = walletRepository.findById(transferRequest.getDestinationWalletId())
				.orElseThrow(() -> new RuntimeException(
						"Destination wallet not found with id: " + transferRequest.getDestinationWalletId()));

		// Check if the source is USD and the destination is VND
		if (!sourceWallet.getCurrency().equals("USD") || !destinationWallet.getCurrency().equals("VND")) {
			throw new IllegalArgumentException("Transfer is only allowed from USD wallet to VND wallet");
		}

		// Calculate the corresponding amount in VND
		BigDecimal amountInVND = transferRequest.getAmount().multiply(transferRequest.getExchangeRate());

		// Check if the source wallet has enough balance
		if (sourceWallet.getBalance().compareTo(transferRequest.getAmount()) < 0) {
			throw new IllegalArgumentException("Insufficient funds in source wallet");
		}

		// Create outgoing transaction from the source wallet
		Transaction outgoingTransaction = new Transaction();
		outgoingTransaction.setTransactionDate(LocalDate.now());
		outgoingTransaction.setAmount(transferRequest.getAmount());
		outgoingTransaction.setWallet(sourceWallet);
		outgoingTransaction.setCategory(categoryRepository.findByName("Outgoing Transfer")
				.stream().findFirst().orElse(null));
		outgoingTransaction.setUser(sourceWallet.getUser());
		outgoingTransaction.setNotes("Transfer Money");
		outgoingTransaction = transactionRepository.save(outgoingTransaction);

		// Create incoming transaction to the destination wallet
		Transaction incomingTransaction = new Transaction();
		incomingTransaction.setTransactionDate(LocalDate.now());
		incomingTransaction.setAmount(amountInVND);
		incomingTransaction.setWallet(destinationWallet);
		incomingTransaction.setCategory(categoryRepository.findByName("Incoming Transfer")
				.stream().findFirst().orElse(null));
		incomingTransaction.setUser(destinationWallet.getUser());
		incomingTransaction.setNotes("Transfer Money");

		if (destinationWallet.getWalletType() == 3) {
			// Fetch saving goals associated with the destination wallet
			List<SavingGoal> goals = saving_goalsRepository.findByWallet_WalletId(destinationWallet.getWalletId());

			// Check if there are saving goals associated with this wallet
			if (!goals.isEmpty()) {
				// A saving goal ID must be provided since goals are available
				Long savingGoalId = transferRequest.getSavingGoalId();
				if (savingGoalId == null || savingGoalId == 0) {
					throw new IllegalArgumentException(
							"A saving goal must be selected for transfers to goal-type wallets");
				}

				// Validate the selected saving goal
				SavingGoal goal = goals.stream()
						.filter(g -> g.getId().equals(savingGoalId))
						.findFirst()
						.orElseThrow(() -> new RuntimeException("Invalid saving goal ID: " + savingGoalId));

				// Update the selected saving goal's current amount
				goal.setCurrentAmount(goal.getCurrentAmount().add(amountInVND));
				saving_goalsRepository.save(goal);

				// Associate the saving goal with the incoming transaction
				incomingTransaction.setSavingGoal(goal);
			} else {
				// No goals available, add amount directly to the wallet balance
				BigDecimal newWalletBalance = destinationWallet.getBalance().add(amountInVND);
				destinationWallet.setBalance(newWalletBalance);
				walletRepository.save(destinationWallet);
			}
		}

		incomingTransaction = transactionRepository.save(incomingTransaction);

		// Update the balance for both wallets
		BigDecimal newSourceBalance = sourceWallet.getBalance().subtract(transferRequest.getAmount());
		sourceWallet.setBalance(newSourceBalance);
		walletRepository.save(sourceWallet);

		BigDecimal newDestinationBalance = destinationWallet.getBalance().add(amountInVND);
		destinationWallet.setBalance(newDestinationBalance);
		walletRepository.save(destinationWallet);

		// Call createIncomeTransaction and createExpenseTransaction with category
		// parameter
		createIncomeTransaction(destinationWallet, amountInVND, incomingTransaction,
				categoryRepository.findByName("Incoming Transfer").stream().findFirst().orElse(null));
		createExpenseTransaction(sourceWallet, transferRequest.getAmount(), outgoingTransaction,
				categoryRepository.findByName("Outgoing Transfer").stream().findFirst().orElse(null));

	}

	@Override
	public Wallet getWalletById(int walletId) {
		return walletRepository.findById(walletId).orElse(null);
	}

	@Override
	public List<Wallet> getWalletsByUserId(int userId) {
		return walletRepository.findByUserId(userId);
	}

	@Override
	public void deleteWallet(int walletId) {
		walletRepository.deleteById(walletId);
	}

	private void createIncomeTransaction(Wallet wallet, BigDecimal amount, Transaction transaction, Category category) {
		Income income = new Income();
		income.setAmount(amount);
		income.setIncomeDate(LocalDate.now());
		income.setUser(wallet.getUser());
		income.setTransaction(transaction);
		income.setWallet(wallet);
		income.setCategory(category);
		// Set other income properties as needed
		incomeRepository.save(income);
	}

	private void createExpenseTransaction(Wallet wallet, BigDecimal amount, Transaction transaction,
			Category category) {
		Expense expense = new Expense();
		expense.setAmount(amount);
		expense.setExpenseDate(LocalDate.now());
		expense.setUser(wallet.getUser());
		expense.setTransaction(transaction);
		expense.setWallet(wallet);
		expense.setCategory(category);
		// Set other income properties as needed
		expenseRepository.save(expense);
	}

}
