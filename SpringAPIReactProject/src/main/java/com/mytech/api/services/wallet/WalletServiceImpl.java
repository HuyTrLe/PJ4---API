package com.mytech.api.services.wallet;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.mytech.api.auth.repositories.UserRepository;
import com.mytech.api.models.category.Category;
import com.mytech.api.models.expense.Expense;
import com.mytech.api.models.income.Income;
import com.mytech.api.models.saving_goals.SavingGoal;
import com.mytech.api.models.transaction.Transaction;
import com.mytech.api.models.wallet.Wallet;
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

	public WalletServiceImpl(WalletRepository walletRepository, UserRepository userRepository,
			TransactionRepository transactionRepository, CategoryRepository categoryRepository,
			IncomeRepository incomeRepository, ExpenseRepository expenseRepository,
			Saving_goalsRepository saving_goalsRepository) {
		this.walletRepository = walletRepository;
		this.transactionRepository = transactionRepository;
		this.categoryRepository = categoryRepository;
		this.incomeRepository = incomeRepository;
		this.expenseRepository = expenseRepository;
		this.saving_goalsRepository = saving_goalsRepository;
	}

	@Override
	public Wallet saveWallet(Wallet wallet) {
		Optional<Wallet> optionalCurrentWallet = walletRepository.findById(wallet.getWalletId());

		// Check if the wallet exists
		if (optionalCurrentWallet.isEmpty()) {
			// Create a new wallet object since it doesn't exist
			return walletRepository.save(wallet);
		}

		Wallet currentWallet = optionalCurrentWallet.get();

		BigDecimal oldBalance = currentWallet.getBalance();

		// Calculate the balance difference using the old and new balances
		BigDecimal balanceDifference = wallet.getBalance().subtract(oldBalance);

		if (currentWallet.getWalletType() == 3 && !wallet.getSavingGoals().isEmpty()) {
			SavingGoal selectedSavingGoal = wallet.getSavingGoals().get(0);
			selectedSavingGoal.setCurrentAmount(selectedSavingGoal.getCurrentAmount().add(balanceDifference));
			saving_goalsRepository.save(selectedSavingGoal);
		}

		// If there's a balance difference and it's not zero, proceed with creating the
		// adjustment transaction
		if (balanceDifference.compareTo(BigDecimal.ZERO) != 0) {
			Transaction adjustmentTransaction = new Transaction();
			adjustmentTransaction.setWallet(currentWallet);
			adjustmentTransaction.setTransactionDate(LocalDate.now());
			adjustmentTransaction.setAmount(balanceDifference.abs());
			adjustmentTransaction.setUser(currentWallet.getUser());

			// Determine the category based on the sign of the balance difference
			Category category = null;
			if (balanceDifference.compareTo(BigDecimal.ZERO) > 0) {
				// Find the income category
				List<Category> incomeCategories = categoryRepository.findByName("Incoming Transfer");
				if (!incomeCategories.isEmpty()) {
					category = incomeCategories.get(0); // Get the first category found
					adjustmentTransaction.setCategory(category);

					// Save the transaction before creating the income
					adjustmentTransaction = transactionRepository.save(adjustmentTransaction);

					Income income = new Income();
					income.setAmount(balanceDifference.abs());
					income.setIncomeDate(LocalDate.now());
					income.setUser(currentWallet.getUser());
					income.setTransaction(adjustmentTransaction);
					income.setWallet(currentWallet);
					income.setCategory(category);

					// Save the income transaction
					incomeRepository.save(income);
				}
			} else {
				// Find the expense category
				List<Category> expenseCategories = categoryRepository.findByName("Outgoing Transfer");
				if (!expenseCategories.isEmpty()) {
					category = expenseCategories.get(0); // Get the first category found
					adjustmentTransaction.setCategory(category);

					// Save the transaction before creating the expense
					adjustmentTransaction = transactionRepository.save(adjustmentTransaction);

					Expense expense = new Expense();
					expense.setAmount(balanceDifference.abs());
					expense.setExpenseDate(LocalDate.now());
					expense.setUser(currentWallet.getUser());
					expense.setTransaction(adjustmentTransaction);
					expense.setWallet(currentWallet);
					expense.setCategory(category);

					// Save the expense transaction
					expenseRepository.save(expense);
				}
			}
		}

		// Note: At this point, 'wallet' already has the new balance set due to
		// modelMapper in the controller
		return walletRepository.save(wallet);
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
}
