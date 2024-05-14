package com.mytech.api.controllers.transaction;

import java.math.BigDecimal;
import java.util.List;
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
import com.mytech.api.models.InsufficientFundsException;
import com.mytech.api.models.budget.ParamBudget;
import com.mytech.api.models.category.CateTypeENum;
import com.mytech.api.models.transaction.FindTransactionParam;
import com.mytech.api.models.transaction.Transaction;
import com.mytech.api.models.transaction.TransactionDTO;
import com.mytech.api.models.transaction.TransactionData;
import com.mytech.api.models.transaction.TransactionReport;
import com.mytech.api.models.transaction.TransactionSavingGoalsView;
import com.mytech.api.models.transaction.TransactionView;
import com.mytech.api.models.wallet.Wallet;
import com.mytech.api.repositories.wallet.WalletRepository;
import com.mytech.api.services.category.CategoryService;
import com.mytech.api.services.recurrence.RecurrenceService;
import com.mytech.api.services.transaction.TransactionService;

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
	WalletRepository walletRepository;
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
		TransactionDTO savedTransactionDTO = transactionService.createTransaction(transactionDTO);
		return ResponseEntity.status(HttpStatus.CREATED).body(savedTransactionDTO);
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

	@GetMapping("/getTop5NewTransactionforWallet/users/{userId}/wallets/{walletId}")
	public ResponseEntity<?> getTop5NewTransactionforWallet(@PathVariable Integer userId,
			@PathVariable Integer walletId) {
		List<TransactionView> transactions = transactionService.getTop5NewTransactionforWallet(userId, walletId);
		if (transactions != null && !transactions.isEmpty()) {
			return ResponseEntity.ok(transactions);
		}
		return ResponseEntity.notFound().build();
	}

	@PostMapping("/getTop5TransactionHightestMoney")
	public ResponseEntity<List<TransactionView>> getTop5TransactionHightestMoney(
			@RequestBody @Valid ParamBudget paramBudget) {
		List<TransactionView> transactions = transactionService.getTop5TransactionHightestMoney(paramBudget);
		if (!transactions.isEmpty()) {
			return ResponseEntity.ok(transactions);
		}
		return ResponseEntity.notFound().build();
	}

	@PostMapping("/GetTransactionWithTime")
	public ResponseEntity<?> GetTransactionWithTime(@RequestBody @Valid ParamBudget paramBudget) {
		List<TransactionData> transactions = transactionService.getTransactionWithTime(paramBudget);
		if (transactions != null && !transactions.isEmpty()) {
			return ResponseEntity.ok(transactions);
		}
		return ResponseEntity.notFound().build();
	}

	@PostMapping("/GetTransactionReport")
	public ResponseEntity<?> GetTransactionReport(@RequestBody @Valid ParamBudget paramBudget) {
		List<TransactionReport> transactions = transactionService.getTransactionReport(paramBudget);
		if (transactions != null && !transactions.isEmpty()) {
			return ResponseEntity.ok(transactions);
		}
		return ResponseEntity.notFound().build();
	}

	@PostMapping("/GetTransactionReportMonth")
	public ResponseEntity<?> GetTransactionReportMonth(@RequestBody @Valid ParamBudget paramBudget) {
		List<TransactionReport> transactions = transactionService.getTransactionReportMonth(paramBudget);
		if (transactions != null && !transactions.isEmpty()) {
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

	@PreAuthorize("#userId == authentication.principal.id")
	@GetMapping("/income/users/{userId}/wallets/{walletId}")
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

	@PreAuthorize("#userId == authentication.principal.id")
	@GetMapping("/expense/users/{userId}/wallets/{walletId}")
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
		walletRepository.save(wallet);

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

	@PostMapping("/FindTransaction")
	public ResponseEntity<?> FindTransaction(@RequestBody @Valid FindTransactionParam param) {
		List<TransactionData> transactions = transactionService.FindTransaction(param);
		if (transactions != null && !transactions.isEmpty()) {
			return ResponseEntity.ok(transactions);
		}
		return ResponseEntity.notFound().build();
	}

	@PostMapping("/getTransactionWithBudget")
	public ResponseEntity<?> getTransactionWithBudget(@RequestBody @Valid ParamBudget paramBudget) {
		List<TransactionData> transactions = transactionService.getTransactionWithBudget(paramBudget);
		if (transactions != null && !transactions.isEmpty()) {
			return ResponseEntity.ok(transactions);
		}
		return ResponseEntity.notFound().build();
	}

	@GetMapping("/savingGoals/{savingGoalId}/users/{userId}")
	@PreAuthorize("#userId == authentication.principal.id")
	public ResponseEntity<?> getTransactionBySavingGoalId(@PathVariable Long savingGoalId, @PathVariable Long userId) {
		List<TransactionSavingGoalsView> transactionDTOs = transactionService
				.getBySavingGoal_IdAndUser_Id(savingGoalId, userId);
		if (transactionDTOs.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(transactionDTOs);
	}

}