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

		if (walletRepository.existsByWalletName(wallet.getWalletName())) {
			throw new IllegalArgumentException("Wallet name already exists");
		}

		if (wallet.getWalletType() == 3 && wallet.getBalance().compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("Wallet Goals cannot have a negative balance");
		}

		if (wallet.getCurrency().equals("USD") && wallet.getBalance().compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("Wallet USD cannot have a negative balance");
		}

		String currency = wallet.getCurrency();
		if (!isValidCurrency(currency)) {
			throw new IllegalArgumentException("Invalid currency");
		}

		if (currency.equals("USD") && walletRepository.existsByCurrency(currency)) {
			throw new IllegalArgumentException("Only one wallet allowed per currency (USD)");
		}

		BigDecimal newBalance = wallet.getBalance().add(wallet.getBalance());
		wallet.setBalance(newBalance);
		walletRepository.save(wallet);
		Transaction transaction = new Transaction();
		transaction.setWallet(wallet);
		transaction.setTransactionDate(LocalDate.now());
		transaction.setAmount(wallet.getBalance().abs());
		transaction.setUser(wallet.getUser());

		if (currency.equals("USD")) {
			Category incomeCategory = categoryRepository.findByName("Incoming Transfer")
					.stream().findFirst().orElse(null);
			if (incomeCategory != null) {
				transaction.setCategory(incomeCategory);
				transaction = transactionRepository.save(transaction);

				// Tạo thu nhập
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
				List<Category> incomeCategories = categoryRepository.findByName("Incoming Transfer");
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
			} else {
				List<Category> expenseCategories = categoryRepository.findByName("Outgoing Transfer");
				if (!expenseCategories.isEmpty()) {
					Category expenseCategory = expenseCategories.get(0);
					transaction.setCategory(expenseCategory);
					transaction = transactionRepository.save(transaction);
					Expense expense = new Expense();
					expense.setAmount(wallet.getBalance().abs());
					expense.setExpenseDate(LocalDate.now());
					expense.setUser(wallet.getUser());
					expense.setTransaction(transaction);
					expense.setWallet(wallet);
					expense.setCategory(expenseCategory);
					expenseRepository.save(expense);
					expenseRepository.save(expense);
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
		existingWallet.setWalletName(walletDTO.getWalletName());
		existingWallet.setBalance(walletDTO.getBalance());
		existingWallet = walletRepository.save(existingWallet);
		BigDecimal balanceDifference = existingWallet.getBalance().subtract(oldBalance);
		if (balanceDifference.compareTo(BigDecimal.ZERO) != 0) {
			if (existingWallet.getWalletType() == 3 && !existingWallet.getSavingGoals().isEmpty()) {
				SavingGoal selectedSavingGoal = existingWallet.getSavingGoals().get(0);
				selectedSavingGoal.setCurrentAmount(selectedSavingGoal.getCurrentAmount().add(balanceDifference));
				saving_goalsRepository.save(selectedSavingGoal);
			}
			Transaction adjustmentTransaction = new Transaction();
			adjustmentTransaction.setWallet(existingWallet);
			adjustmentTransaction.setTransactionDate(LocalDate.now());
			adjustmentTransaction.setAmount(balanceDifference.abs());
			adjustmentTransaction.setUser(existingWallet.getUser());
			Category category = null;
			if (balanceDifference.compareTo(BigDecimal.ZERO) > 0) {
				category = categoryRepository.findByName("Incoming Transfer")
						.stream().findFirst().orElse(null);
			} else {
				category = categoryRepository.findByName("Outgoing Transfer")
						.stream().findFirst().orElse(null);
			}
			if (category != null) {
				adjustmentTransaction.setCategory(category);
				transactionRepository.save(adjustmentTransaction);
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
				.orElseThrow(
						() -> new RuntimeException(
								"Destination wallet not found with id: " + transferRequest.getDestinationWalletId()));

		// Kiểm tra nếu ví nguồn là USD và ví đích là VND
		if (!sourceWallet.getCurrency().equals("USD") || !destinationWallet.getCurrency().equals("VND")) {
			throw new IllegalArgumentException("Transfer is only allowed from USD wallet to VND wallet");
		}

		// Tính toán số tiền tương ứng trong VND
		BigDecimal amountInVND = transferRequest.getAmount().multiply(transferRequest.getExchangeRate());

		// Kiểm tra số dư của ví nguồn đủ để thực hiện giao dịch không
		if (sourceWallet.getBalance().compareTo(transferRequest.getAmount()) < 0) {
			throw new IllegalArgumentException("Insufficient funds in source wallet");
		}

		// Tạo giao dịch chuyển tiền đi từ ví nguồn
		Transaction outgoingTransaction = new Transaction();
		outgoingTransaction.setTransactionDate(LocalDate.now());
		outgoingTransaction.setAmount(transferRequest.getAmount());
		outgoingTransaction.setWallet(sourceWallet);
		outgoingTransaction.setCategory(categoryRepository.findByName("Outgoing Transfer")
				.stream().findFirst().orElse(null));
		outgoingTransaction.setUser(sourceWallet.getUser());
		outgoingTransaction.setNotes("Transfer Money");
		transactionRepository.save(outgoingTransaction);

		// Tạo giao dịch chuyển tiền đến ví đích
		Transaction incomingTransaction = new Transaction();
		incomingTransaction.setTransactionDate(LocalDate.now());
		incomingTransaction.setAmount(amountInVND);
		incomingTransaction.setWallet(destinationWallet);
		incomingTransaction.setCategory(categoryRepository.findByName("Incoming Transfer")
				.stream().findFirst().orElse(null));
		incomingTransaction.setUser(destinationWallet.getUser());
		incomingTransaction.setNotes("Transfer Money");
		transactionRepository.save(incomingTransaction);

		// Cập nhật số dư cho cả hai ví
		BigDecimal newSourceBalance = sourceWallet.getBalance().subtract(transferRequest.getAmount());
		sourceWallet.setBalance(newSourceBalance);
		walletRepository.save(sourceWallet);

		BigDecimal newDestinationBalance = destinationWallet.getBalance().add(amountInVND);
		destinationWallet.setBalance(newDestinationBalance);
		walletRepository.save(destinationWallet);
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
