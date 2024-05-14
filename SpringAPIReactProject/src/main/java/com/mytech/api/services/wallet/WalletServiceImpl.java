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

		if ("USD".equals(wallet.getCurrency()) && walletDTO.getBalance().compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("Wallet USD cannot have a negative balance");
		}

		if (wallet.getCurrency().equals("USD")
				&& walletRepository.existsByCurrencyAndUserId(wallet.getCurrency(), wallet.getUser().getId())) {
			throw new IllegalArgumentException("Only one wallet allowed per currency (USD) for each user");
		}

		String currency = wallet.getCurrency();
		if (!isValidCurrency(currency)) {
			throw new IllegalArgumentException("Invalid currency");
		}

		BigDecimal newBalance = wallet.getBalance();
		wallet.setBalance(newBalance);
		walletRepository.save(wallet);

		if (newBalance.compareTo(BigDecimal.ZERO) != 0) {
			Transaction transaction = new Transaction();
			transaction.setWallet(wallet);
			transaction.setTransactionDate(LocalDate.now());
			transaction.setAmount(wallet.getBalance().abs());
			transaction.setUser(wallet.getUser());

			if (currency.equals("USD")) {
				List<Category> incomeCategories = categoryRepository.findByNameAndUserId("Incoming Transfer",
						wallet.getUser().getId());
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
					List<Category> incomeCategories = categoryRepository.findByNameAndUserId("Incoming Transfer",
							wallet.getUser().getId());
					if (!incomeCategories.isEmpty()) {
						Category incomeCategory = incomeCategories.get(0);
						transaction.setCategory(incomeCategory);
						transaction = transactionRepository.save(transaction);
						createIncomeTransaction(wallet, newBalance, transaction, incomeCategory);
					}
				} else {
					List<Category> expenseCategories = categoryRepository.findByNameAndUserId("Outgoing Transfer",
							wallet.getUser().getId());
					if (!expenseCategories.isEmpty()) {
						Category expenseCategory = expenseCategories.get(0);
						transaction.setCategory(expenseCategory);
						transaction = transactionRepository.save(transaction);
						createExpenseTransaction(wallet, newBalance, transaction, expenseCategory);
					}
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

		// Calculate balance difference
		BigDecimal balanceDifference = existingWallet.getBalance().subtract(oldBalance);

		// If balance difference is not zero, handle accordingly
		if (balanceDifference.compareTo(BigDecimal.ZERO) != 0) {
			if (existingWallet.getWalletType() != 3) {
				// Create transaction for the balance adjustment
				Transaction balanceTransaction = new Transaction();
				balanceTransaction.setWallet(existingWallet);
				balanceTransaction.setTransactionDate(LocalDate.now());
				balanceTransaction.setAmount(balanceDifference.abs());
				balanceTransaction.setUser(existingWallet.getUser());

				// Determine category based on balance difference
				Category category;
				if (balanceDifference.compareTo(BigDecimal.ZERO) > 0) {
					List<Category> incomeCategories = categoryRepository.findByNameAndUserId("Incoming Transfer",
							existingWallet.getUser().getId());
					category = !incomeCategories.isEmpty() ? incomeCategories.get(0) : null;
					balanceTransaction.setCategory(category);
					transactionRepository.save(balanceTransaction);
					createIncomeTransaction(existingWallet, balanceDifference.abs(), balanceTransaction, category);
				} else {
					List<Category> expenseCategories = categoryRepository.findByNameAndUserId("Outgoing Transfer",
							existingWallet.getUser().getId());
					category = !expenseCategories.isEmpty() ? expenseCategories.get(0) : null;
					balanceTransaction.setCategory(category);
					transactionRepository.save(balanceTransaction);
					createExpenseTransaction(existingWallet, balanceDifference.abs(), balanceTransaction, category);
				}
			} else {
				throw new IllegalArgumentException("Cannot update goals wallet");
			}
		}
		walletRepository.save(existingWallet);
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

		List<Category> categories = categoryRepository.findByNameAndUserId("Incoming Transfer",
				destinationWallet.getUser().getId());
		Category categoryOutgoing = categories.isEmpty() ? null : categories.get(0);
		List<Category> categories2 = categoryRepository.findByNameAndUserId("Outgoing Transfer",
				sourceWallet.getUser().getId());
		Category categoryIncoming = categories2.isEmpty() ? null : categories2.get(0);
		// Create outgoing transaction from the source wallet
		Transaction outgoingTransaction = new Transaction();
		outgoingTransaction.setTransactionDate(LocalDate.now());
		outgoingTransaction.setAmount(transferRequest.getAmount());
		outgoingTransaction.setWallet(sourceWallet);
		outgoingTransaction.setCategory(categoryOutgoing);
		outgoingTransaction.setUser(sourceWallet.getUser());
		outgoingTransaction.setNotes("Transfer Money");
		outgoingTransaction = transactionRepository.save(outgoingTransaction);

		// Create incoming transaction to the destination wallet
		Transaction incomingTransaction = new Transaction();
		incomingTransaction.setTransactionDate(LocalDate.now());
		incomingTransaction.setAmount(amountInVND);
		incomingTransaction.setWallet(destinationWallet);
		incomingTransaction.setCategory(categoryIncoming);
		incomingTransaction.setUser(destinationWallet.getUser());
		incomingTransaction.setNotes("Transfer Money");

		if (destinationWallet.getWalletType() != 3) {

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
			createIncomeTransaction(destinationWallet, amountInVND, incomingTransaction, categoryIncoming);
			createExpenseTransaction(sourceWallet, transferRequest.getAmount(), outgoingTransaction, categoryOutgoing);
		} else {
			throw new IllegalArgumentException("Cannot transfer to goals wallet");
		}

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
