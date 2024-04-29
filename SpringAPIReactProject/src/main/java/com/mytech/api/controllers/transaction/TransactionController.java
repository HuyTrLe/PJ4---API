package com.mytech.api.controllers.transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mytech.api.auth.repositories.UserRepository;
import com.mytech.api.auth.services.MyUserDetails;
import com.mytech.api.models.category.CateTypeENum;
import com.mytech.api.models.category.Category;
import com.mytech.api.models.expense.Expense;
import com.mytech.api.models.income.Income;
import com.mytech.api.models.transaction.Transaction;
import com.mytech.api.models.transaction.TransactionDTO;
import com.mytech.api.models.user.User;
import com.mytech.api.models.user.UserDTO;
import com.mytech.api.models.wallet.Wallet;
import com.mytech.api.models.wallet.WalletDTO;
import com.mytech.api.services.category.CategoryService;
import com.mytech.api.services.recurrence.RecurrenceService;
import com.mytech.api.services.transaction.TransactionService;
import com.mytech.api.services.wallet.WalletService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
	@Autowired
	TransactionService transactionService;
	@Autowired
	CategoryService categoryService;
	@Autowired
	UserRepository userRepository;
	@Autowired
	WalletService walletService;
	@Autowired
	RecurrenceService recurrenceService;
	@Autowired
	ModelMapper modelMapper;

	@PreAuthorize("#transactionDTO.user.id == authentication.principal.id")
	@PostMapping("/create")
	public ResponseEntity<?> createTransaction(@RequestBody @Valid TransactionDTO transactionDTO,
			BindingResult result) {
		if (result.hasErrors()) {
			String errors = result.getFieldErrors().stream().map(error -> error.getDefaultMessage())
					.collect(Collectors.joining(", "));
			System.out.println(errors);
			return ResponseEntity.badRequest().body(errors);
		}
		Transaction transaction = modelMapper.map(transactionDTO, Transaction.class);
		UserDTO userDTO = transactionDTO.getUser();
		if (userDTO == null || userDTO.getId() == null) {
			return new ResponseEntity<>("User ID must be provided", HttpStatus.BAD_REQUEST);
		}
		Optional<User> existingUser = userRepository.findById(userDTO.getId());
		if (!existingUser.isPresent()) {
			return new ResponseEntity<>("User not found with id: " + userDTO.getId(), HttpStatus.NOT_FOUND);
		}
		Wallet existingWallet = walletService.getWalletById(transactionDTO.getWallet().getWalletId());
		if (existingWallet == null) {
			return new ResponseEntity<>("Wallet not found with id: " + transactionDTO.getWallet().getWalletId(),
					HttpStatus.NOT_FOUND);
		}
		WalletDTO existingWalletDTO = modelMapper.map(existingWallet, WalletDTO.class);
		try {
			existingWalletDTO.addTransactionBalance(transactionDTO);
			walletService.saveWallet(modelMapper.map(existingWalletDTO, Wallet.class));
		} catch (RuntimeException e) {
			return new ResponseEntity<>("Balance is not enough to complete this transaction", HttpStatus.BAD_REQUEST);
		}
		Category existingCategory = categoryService.getByCateId(transactionDTO.getCategory().getId());
		if (existingCategory == null) {
			return new ResponseEntity<>("Category not found with id: " + transactionDTO.getCategory().getId(),
					HttpStatus.NOT_FOUND);
		}
		transaction.setCategory(existingCategory);
		transaction.setUser(existingUser.get());
		transaction.setWallet(existingWallet);
		transaction.setAmount(transaction.getAmount());
		transaction.setTransactionDate(transaction.getTransactionDate());
		if (existingCategory.getType() == CateTypeENum.INCOME) {
			Income income = new Income();
			income.setUser(existingUser.get());
			income.setWallet(existingWallet);
			income.setAmount(transaction.getAmount());
			income.setIncomeDate(transaction.getTransactionDate());
			income.setCategory(existingCategory);
			income.setTransaction(transaction);
			transaction.setIncome(income);
		} else {
			Expense expense = new Expense();
			expense.setUser(existingUser.get());
			expense.setWallet(existingWallet);
			expense.setAmount(transaction.getAmount());
			expense.setExpenseDate(transaction.getTransactionDate());
			expense.setCategory(existingCategory);
			expense.setTransaction(transaction);
			transaction.setExpense(expense);
		}
		Transaction savedTransaction = transactionService.saveTransaction(transaction);
		TransactionDTO savedTransactionDTO = modelMapper.map(savedTransaction, TransactionDTO.class);
		return ResponseEntity.ok(savedTransactionDTO);
	}

	@GetMapping("/{transactionId}")
	public ResponseEntity<?> getTransactionById(@PathVariable Integer transactionId) {
		Transaction transaction = transactionService.getTransactionById(transactionId);
		if (transaction != null) {
			TransactionDTO transactionDTO = modelMapper.map(transaction, TransactionDTO.class);
			return ResponseEntity.ok(transactionDTO);
		}
		return ResponseEntity.notFound().build();
	}

	@GetMapping("/users/{userId}")
	public ResponseEntity<Page<TransactionDTO>> getAllTransactionsForUser(@PathVariable int userId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

		PageRequest pageable = PageRequest.of(page, size);

		Page<TransactionDTO> transactionsPage = transactionService.getAllTransactionsByUserId(userId, pageable)
				.map(transaction -> {
					TransactionDTO transactionDTO = modelMapper.map(transaction, TransactionDTO.class);
					return transactionDTO;
				});

		return new ResponseEntity<>(transactionsPage, HttpStatus.OK);
	}

	@PreAuthorize("#transactionDTO.user.id == authentication.principal.id")
	@PutMapping("/update/{transactionId}")
	public ResponseEntity<?> updateTransaction(@PathVariable Integer transactionId,
			@RequestBody @Valid TransactionDTO transactionDTO, BindingResult result) {
		if (result.hasErrors()) {
			String errors = result.getFieldErrors().stream().map(error -> error.getDefaultMessage())
					.collect(Collectors.joining(", "));
			return ResponseEntity.badRequest().body(errors);
		}
		Transaction transaction = modelMapper.map(transactionDTO, Transaction.class);
		Transaction existingTransaction = transactionService.getTransactionById(transactionId);
		if (existingTransaction == null) {
			return ResponseEntity.notFound().build();
		}

		UserDTO userDTO = transactionDTO.getUser();
		if (userDTO == null || userDTO.getId() == null) {
			return new ResponseEntity<>("User ID must be provided", HttpStatus.BAD_REQUEST);
		}
		Optional<User> existingUser = userRepository.findById(userDTO.getId());
		if (!existingUser.isPresent()) {
			return new ResponseEntity<>("User not found with id: " + userDTO.getId(), HttpStatus.NOT_FOUND);
		}

		Wallet existingWallet = walletService.getWalletById(transactionDTO.getWallet().getWalletId());
		if (existingWallet == null) {
			return new ResponseEntity<>("Wallet not found with id: " + transactionDTO.getWallet().getWalletId(),
					HttpStatus.NOT_FOUND);
		}
		Wallet wallet = existingTransaction.getWallet();
		BigDecimal newBalance = calculateNewWalletBalance(wallet.getBalance(), existingTransaction.getAmount(),
				transaction.getAmount());
		if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
			return ResponseEntity.badRequest().body("Insufficient funds in the wallet");
		}
		wallet.setBalance(newBalance);
		walletService.saveWallet(wallet);

		Category existingCategory = categoryService.getByCateId(transactionDTO.getCategory().getId());
		if (existingCategory == null) {
			return new ResponseEntity<>("Category not found with id: " + transactionDTO.getCategory().getId(),
					HttpStatus.NOT_FOUND);
		}

		existingTransaction.setCategory(existingCategory);
		existingTransaction.setUser(existingUser.get());
		existingTransaction.setWallet(existingWallet);
		existingTransaction.setAmount(transaction.getAmount());
		existingTransaction.setTransactionDate(transaction.getTransactionDate());
		if (existingCategory.getType() == CateTypeENum.INCOME) {
			Income income = existingTransaction.getIncome();
			income.setUser(existingUser.get());
			income.setWallet(existingWallet);
			income.setAmount(transaction.getAmount());
			income.setIncomeDate(transaction.getTransactionDate());
			income.setCategory(existingCategory);
			income.setTransaction(transaction);
			transaction.setIncome(income);
		} else {
			Expense expense = existingTransaction.getExpense();
			expense.setUser(existingUser.get());
			expense.setWallet(existingWallet);
			expense.setAmount(transaction.getAmount());
			expense.setExpenseDate(transaction.getTransactionDate());
			expense.setCategory(existingCategory);
			expense.setTransaction(transaction);
			transaction.setExpense(expense);
		}
		Transaction updatedTransaction = transactionService.saveTransaction(existingTransaction);
		TransactionDTO updatedTransactionDTO = modelMapper.map(updatedTransaction, TransactionDTO.class);
		return ResponseEntity.ok(updatedTransactionDTO);
	}

	private BigDecimal calculateNewWalletBalance(BigDecimal currentBalance, BigDecimal oldAmount,
			BigDecimal newAmount) {
		if (newAmount.compareTo(oldAmount) > 0) { // newAmount > oldAmount
			return currentBalance.subtract(newAmount.subtract(oldAmount));
		} else { // newAmount <= oldAmount
			return currentBalance.add(oldAmount.subtract(newAmount));
		}
	}

	@GetMapping("/allWallets/users/{userId}")
	public ResponseEntity<List<TransactionDTO>> getAllTransactionsForAllWallet(@PathVariable int userId) {
		List<TransactionDTO> transactions = transactionService.getAllTransactionsByAllWallet(userId)
				.stream()
				.map(transaction -> {
					TransactionDTO transactionDTO = modelMapper.map(transaction, TransactionDTO.class);
					return transactionDTO;
				})
				.collect(Collectors.toList());

		return new ResponseEntity<>(transactions, HttpStatus.OK);
	}

	@GetMapping("/allIncome/users/{userId}")
	public ResponseEntity<List<TransactionDTO>> getIncomeByUserId(@PathVariable Integer userId) {
		List<TransactionDTO> incomeTransactions = transactionService.getIncomeByUserIdAndCategoryType(userId,
				CateTypeENum.INCOME).stream()
				.map(transaction -> {
					TransactionDTO transactionDTO = modelMapper.map(transaction, TransactionDTO.class);
					return transactionDTO;
				})
				.collect(Collectors.toList());
		;
		if (incomeTransactions.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(incomeTransactions);
	}

	@GetMapping("/allExpense/users/{userId}")
	public ResponseEntity<List<TransactionDTO>> getExpenseByUserId(@PathVariable Integer userId) {
		List<TransactionDTO> expenseTransactions = transactionService.getExpenseByUserIdAndCategoryType(userId,
				CateTypeENum.EXPENSE).stream()
				.map(transaction -> {
					TransactionDTO transactionDTO = modelMapper.map(transaction, TransactionDTO.class);
					return transactionDTO;
				})
				.collect(Collectors.toList());
		if (expenseTransactions.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(expenseTransactions);
	}

	@GetMapping("/income/users/{userId}/wallets/{walletId}")
	@PreAuthorize("#userId == authentication.principal.id")
	public ResponseEntity<List<TransactionDTO>> getTotalIncomeByWalletId(@PathVariable Integer userId,
			@PathVariable Integer walletId) {
		List<TransactionDTO> totalIncome = transactionService.getTotalIncomeByWalletId(userId, walletId,
				CateTypeENum.INCOME).stream()
				.map(transaction -> {
					TransactionDTO transactionDTO = modelMapper.map(transaction, TransactionDTO.class);
					return transactionDTO;
				})
				.collect(Collectors.toList());
		if (totalIncome.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(totalIncome);
	}

	@GetMapping("/expense/users/{userId}/wallets/{walletId}")
	@PreAuthorize("#userId == authentication.principal.id")
	public ResponseEntity<List<TransactionDTO>> getTotalExpenseByWalletId(@PathVariable Integer userId,
			@PathVariable Integer walletId) {
		List<TransactionDTO> totalExpense = transactionService
				.getTotalExpenseByWalletId(userId, walletId, CateTypeENum.EXPENSE)
				.stream()
				.map(transaction -> {
					TransactionDTO transactionDTO = modelMapper.map(transaction, TransactionDTO.class);
					return transactionDTO;
				})
				.collect(Collectors.toList());
		if (totalExpense.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(totalExpense);
	}

	@DeleteMapping("/delete/{transactionId}")
	public ResponseEntity<?> deleteTransaction(@PathVariable Integer transactionId, Authentication authentication) {
		Transaction transaction = transactionService.getTransactionById(transactionId);

		if (transaction == null) {
			return ResponseEntity.notFound().build();
		}

		MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
		if (!transaction.getUser().getId().equals(userDetails.getId())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body("You are not authorized to delete this transaction.");
		}

		Wallet wallet = transaction.getWallet();

		BigDecimal newBalance;
		if ("INCOME".equalsIgnoreCase(transaction.getCategory().getType().toString())) {
			newBalance = calculateDeleteTransWalletBalance(wallet.getBalance(), transaction.getAmount(),
					BigDecimal.ZERO);
		} else {
			newBalance = calculateDeleteTransWalletBalance(wallet.getBalance(), BigDecimal.ZERO,
					transaction.getAmount());
		}

		wallet.setBalance(newBalance);
		walletService.saveWallet(wallet);

		transactionService.deleteTransaction(transactionId);
		return ResponseEntity.noContent().build();
	}

	private BigDecimal calculateDeleteTransWalletBalance(BigDecimal currentBalance, BigDecimal incomeAmount,
			BigDecimal expenseAmount) {
		return currentBalance.subtract(incomeAmount).add(expenseAmount);
	}

	@GetMapping("/wallets/{walletId}/users/{userId}")
	@PreAuthorize("#userId == authentication.principal.id")
	public ResponseEntity<List<TransactionDTO>> getTransactionsByWalletId(@PathVariable int userId,
			@PathVariable Integer walletId) {
		List<Transaction> transactions = transactionService.getTransactionsByWalletId(userId, walletId);
		List<TransactionDTO> transactionDTOs = transactions.stream()
				.map(transaction -> modelMapper.map(transaction, TransactionDTO.class))
				.collect(Collectors.toList());
		return ResponseEntity.ok(transactionDTOs);
	}

}