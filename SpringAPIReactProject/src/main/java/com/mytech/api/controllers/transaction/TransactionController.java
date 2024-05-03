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
import org.springframework.web.bind.annotation.ExceptionHandler;
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
import com.mytech.api.models.transaction.TransactionView;
import com.mytech.api.models.user.User;
import com.mytech.api.models.wallet.Wallet;
import com.mytech.api.services.category.CategoryService;
import com.mytech.api.services.recurrence.RecurrenceService;
import com.mytech.api.services.transaction.TransactionService;
import com.mytech.api.services.wallet.WalletService;
import com.mytech.api.models.InsufficientFundsException;

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
	
	 @ExceptionHandler(InsufficientFundsException.class)
	    public ResponseEntity<String> handleInsufficientFunds(InsufficientFundsException ex) {
	        // Log the error message if needed
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
	 }

	@PreAuthorize("#transactionDTO.userId == authentication.principal.id")
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
		if (transactionDTO.getUserId() == 0) {
			return new ResponseEntity<>("User ID must be provided", HttpStatus.BAD_REQUEST);
		}
		Optional<User> existingUser = userRepository.findById(transactionDTO.getUserId());
		if (!existingUser.isPresent()) {
			return new ResponseEntity<>("User not found with id: " + transactionDTO.getUserId(), HttpStatus.NOT_FOUND);
		}
		Wallet existingWallet = walletService.getWalletById(transactionDTO.getWalletId());
		if (existingWallet == null) {
			return new ResponseEntity<>("Wallet not found with id: " + transactionDTO.getWalletId(),
					HttpStatus.NOT_FOUND);
		}
		Category existingCategory = categoryService.getByCateId(transactionDTO.getCategoryId());
		if (existingCategory == null) {
			return new ResponseEntity<>("Category not found with id: " + transactionDTO.getCategoryId(),
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

	@GetMapping("/getTop5NewTransaction/users/{userId}")
	public ResponseEntity<?> getTop5NewTransaction(@PathVariable Integer userId) {
		List<TransactionView> transactions = transactionService.getTop5NewTransaction(userId);
		if (transactions != null && !transactions.isEmpty()) {
			return ResponseEntity.ok(transactions);
		}
		return ResponseEntity.notFound().build();
	}

	@GetMapping("/getTop5TransactionHightestMoney/users/{userId}")
	public ResponseEntity<List<TransactionView>> getTop5TransactionHightestMoney(@PathVariable Integer userId) {
		List<TransactionView> transactions = transactionService.getTop5TransactionHightestMoney(userId);
		if (!transactions.isEmpty()) {
			return ResponseEntity.ok(transactions);
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

	@PutMapping("/update/{transactionId}")
	@PreAuthorize("#transactionDTO.userId == authentication.principal.id")
	public ResponseEntity<?> updateTransaction(@PathVariable Integer transactionId,
			@RequestBody @Valid TransactionDTO transactionDTO,
			BindingResult result) {
		System.out.println("Received DTO for update: " + transactionDTO);

		if (result.hasErrors()) {
			String errors = result.getFieldErrors().stream()
					.map(error -> error.getDefaultMessage())
					.collect(Collectors.joining(", "));
			return ResponseEntity.badRequest().body(errors);
		}

		TransactionDTO updatedTransactionDTO = transactionService.updateTransaction(transactionId, transactionDTO);
		return ResponseEntity.ok(updatedTransactionDTO);
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