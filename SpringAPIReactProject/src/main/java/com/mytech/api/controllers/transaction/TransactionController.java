package com.mytech.api.controllers.transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.mytech.api.auth.repositories.UserRepository;
import com.mytech.api.models.category.Category;
import com.mytech.api.models.recurrence.Recurrence;
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

	private final TransactionService transactionService;
	private final CategoryService categoryService;
	private final UserRepository userRepository;
	private final WalletService walletService;
	private final RecurrenceService recurrenceService;
	private final ModelMapper modelMapper;

	public TransactionController(TransactionService transactionService, CategoryService categoryService,
			WalletService walletService, RecurrenceService recurrenceService, UserRepository userRepository,
			ModelMapper modelMapper) {
		this.transactionService = transactionService;
		this.userRepository = userRepository;
		this.categoryService = categoryService;
		this.walletService = walletService;
		this.recurrenceService = recurrenceService;
		this.modelMapper = modelMapper;
	}

	@PostMapping
	public ResponseEntity<?> createTransaction(@RequestBody @Valid TransactionDTO transactionDTO,
			BindingResult result) {
		if (result.hasErrors()) {
			String errors = result.getFieldErrors().stream().map(error -> error.getDefaultMessage())
					.collect(Collectors.joining(", "));
			System.out.println(errors);
			return ResponseEntity.badRequest().body(errors);
		}

		UserDTO userDTO = transactionDTO.getUser();
		if (userDTO == null || userDTO.getId() == null) {
			return new ResponseEntity<>("User ID must be provided", HttpStatus.BAD_REQUEST);
		}

		Optional<User> existingUser = userRepository.findById(userDTO.getId());
		if (!existingUser.isPresent()) {
			return new ResponseEntity<>("User not found with id: " + userDTO.getId(), HttpStatus.NOT_FOUND);
		}

		Transaction transaction = modelMapper.map(transactionDTO, Transaction.class);
		Transaction savedTransaction = transactionService.saveTransaction(transaction);

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

		if (transactionDTO.getRecurrence() != null) {
			if (transactionDTO.getRecurrence().getRecurrenceId() > 0) {
				Recurrence existingRecurrence = recurrenceService
						.findRecurrenceById(transactionDTO.getRecurrence().getRecurrenceId());
				if (existingRecurrence == null) {
					return new ResponseEntity<>(
							"Recurrence not found with id: " + transactionDTO.getRecurrence().getRecurrenceId(),
							HttpStatus.NOT_FOUND);
				}
				transaction.setRecurrence(existingRecurrence);
			}
		}

		transaction.setCategory(existingCategory);

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

	@PutMapping("/{transactionId}")
	public ResponseEntity<?> updateTransaction(@PathVariable Integer transactionId,
			@RequestBody @Valid TransactionDTO transactionDTO, BindingResult result) {
		if (result.hasErrors()) {
			String errors = result.getFieldErrors().stream().map(error -> error.getDefaultMessage())
					.collect(Collectors.joining(", "));
			return ResponseEntity.badRequest().body(errors);
		}

		Transaction updatedTransaction = modelMapper.map(transactionDTO, Transaction.class);
		updatedTransaction.setTransactionId(transactionId);

		// Update wallet balance
		Wallet wallet = updatedTransaction.getWallet();
		BigDecimal newBalance = calculateNewWalletBalance(wallet.getBalance(), transactionDTO.getAmount(),
				transactionDTO.getAmount());
		wallet.setBalance(newBalance); // setBalance accepts BigDecimal
		walletService.saveWallet(wallet);

		updatedTransaction = transactionService.saveTransaction(updatedTransaction);

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

	@GetMapping("/income/{userId}")
	public ResponseEntity<BigDecimal> getTotalIncomeByUserId(@PathVariable Integer userId) {
		BigDecimal totalIncome = transactionService.getTotalIncomeByUserId(userId);
		return ResponseEntity.ok(totalIncome);
	}

	@GetMapping("/expense/{userId}")
	public ResponseEntity<BigDecimal> getTotalExpenseByUserId(@PathVariable Integer userId) {
		BigDecimal totalExpense = transactionService.getTotalExpenseByUserId(userId);
		return ResponseEntity.ok(totalExpense);
	}

	@DeleteMapping("/{transactionId}")
	public ResponseEntity<?> deleteTransaction(@PathVariable Integer transactionId) {
	    Transaction transaction = transactionService.getTransactionById(transactionId);
	    
	    if (transaction == null) {
	        return ResponseEntity.notFound().build();
	    }

	    Wallet wallet = transaction.getWallet();

	    BigDecimal newBalance = calculateDeleteTransWalletBalance(wallet.getBalance(), transaction.getAmount(), BigDecimal.ZERO);
	    wallet.setBalance(newBalance); 
	    walletService.saveWallet(wallet);

	    transactionService.deleteTransaction(transactionId);
	    return ResponseEntity.noContent().build();
	}

	private BigDecimal calculateDeleteTransWalletBalance(BigDecimal currentBalance, BigDecimal oldAmount, BigDecimal newAmount) {
	    return currentBalance.add(oldAmount);
	}
	
	@GetMapping("/category/{categoryId}")
	public ResponseEntity<List<TransactionDTO>> getTransactionsByCategoryId(@PathVariable Integer categoryId) {
	    List<Transaction> transactions = transactionService.getTransactionsByCategoryId(categoryId);
	    List<TransactionDTO> transactionDTOs = transactions.stream()
	            .map(transaction -> modelMapper.map(transaction, TransactionDTO.class))
	            .collect(Collectors.toList());
	    return ResponseEntity.ok(transactionDTOs);
	}


}