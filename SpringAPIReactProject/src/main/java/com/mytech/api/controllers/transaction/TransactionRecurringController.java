package com.mytech.api.controllers.transaction;

import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.mytech.api.models.category.Category;
import com.mytech.api.models.recurrence.Recurrence;
import com.mytech.api.models.recurrence.RecurrenceConverter;
import com.mytech.api.models.transaction.TransactionRecurring;
import com.mytech.api.models.transaction.TransactionRecurringDTO;
import com.mytech.api.models.user.User;
import com.mytech.api.models.user.UserDTO;
import com.mytech.api.models.wallet.Wallet;
import com.mytech.api.services.category.CategoryService;
import com.mytech.api.services.recurrence.RecurrenceService;
import com.mytech.api.services.transaction.TransactionRecurringService;
import com.mytech.api.services.wallet.WalletService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transactionsRecurring")
public class TransactionRecurringController {

    @Autowired
    TransactionRecurringService transactionRecurringService;
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
    @Autowired
    RecurrenceConverter recurrenceConverter;

    @PostMapping("/create")
    public ResponseEntity<?> createTransaction(@RequestBody @Valid TransactionRecurringDTO transactionRecurringDTO,
            BindingResult result) {
        if (result.hasErrors()) {
            String errors = result.getFieldErrors().stream().map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            System.out.println(errors);
            return ResponseEntity.badRequest().body(errors);
        }
        TransactionRecurring transactionRecurring = modelMapper.map(transactionRecurringDTO,
                TransactionRecurring.class);

        UserDTO userDTO = transactionRecurringDTO.getUser();
        if (userDTO == null || userDTO.getId() == null) {
            return new ResponseEntity<>("User ID must be provided", HttpStatus.BAD_REQUEST);
        }

        Optional<User> existingUser = userRepository.findById(userDTO.getId());
        if (!existingUser.isPresent()) {
            return new ResponseEntity<>("User not found with id: " + userDTO.getId(), HttpStatus.NOT_FOUND);
        }
        Wallet existingWallet = walletService.getWalletById(transactionRecurringDTO.getWallet().getWalletId());
        if (existingWallet == null) {
            return new ResponseEntity<>(
                    "Wallet not found with id: " + transactionRecurringDTO.getWallet().getWalletId(),
                    HttpStatus.NOT_FOUND);
        }

        Category existingCategory = categoryService.getByCateId(transactionRecurringDTO.getCategory().getId());
        if (existingCategory == null) {
            return new ResponseEntity<>("Category not found with id: " + transactionRecurringDTO.getCategory().getId(),
                    HttpStatus.NOT_FOUND);
        }

        Recurrence newRecurrence = recurrenceConverter.convertToEntity(transactionRecurringDTO.getRecurrence());
        newRecurrence.setStartDate(transactionRecurringDTO.getRecurrence().getStartDate());
        newRecurrence.setUser(existingUser.get());
        Recurrence savedRecurrence = recurrenceService.saveRecurrence(newRecurrence);
        transactionRecurring.setRecurrence(savedRecurrence);
        transactionRecurring.setCategory(existingCategory);
        transactionRecurring.setUser(existingUser.get());
        transactionRecurring.setWallet(existingWallet);
        TransactionRecurring savedTransaction = transactionRecurringService
                .saveTransactionsRecurring(transactionRecurring);
        TransactionRecurringDTO savedTransactionDTO = modelMapper.map(savedTransaction, TransactionRecurringDTO.class);
        return ResponseEntity.ok(savedTransactionDTO);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<?> getTransactionById(@PathVariable Integer transactionId) {
        TransactionRecurring transaction = transactionRecurringService.getTransactionsRecurringById(transactionId);
        if (transaction != null) {
            TransactionRecurringDTO transactionDTO = modelMapper.map(transaction, TransactionRecurringDTO.class);
            return ResponseEntity.ok(transactionDTO);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<Page<TransactionRecurringDTO>> getAllTransactionsForUser(@PathVariable int userId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

        PageRequest pageable = PageRequest.of(page, size);

        Page<TransactionRecurringDTO> transactionsPage = transactionRecurringService
                .getAllTransactionsRecurringByUserId(userId, pageable)
                .map(transaction -> {
                    TransactionRecurringDTO transactionRecurringDTO = modelMapper.map(transaction,
                            TransactionRecurringDTO.class);
                    return transactionRecurringDTO;
                });

        return new ResponseEntity<>(transactionsPage, HttpStatus.OK);
    }

    @PutMapping("/update/{transactionId}")
    public ResponseEntity<?> updateTransaction(@PathVariable Integer transactionId,
            @RequestBody @Valid TransactionRecurringDTO transactionRecurringDTO, BindingResult result) {
        if (result.hasErrors()) {
            String errors = result.getFieldErrors().stream().map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            System.out.println(errors);
            return ResponseEntity.badRequest().body(errors);
        }
        Optional<TransactionRecurring> existingTransactionRecurring = Optional
                .ofNullable(transactionRecurringService.getTransactionsRecurringById(transactionId));
        if (!existingTransactionRecurring.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        UserDTO userDTO = transactionRecurringDTO.getUser();
        if (userDTO == null || userDTO.getId() == null) {
            return new ResponseEntity<>("User ID must be provided", HttpStatus.BAD_REQUEST);
        }

        Optional<User> existingUser = userRepository.findById(userDTO.getId());
        if (!existingUser.isPresent()) {
            return new ResponseEntity<>("User not found with id: " + userDTO.getId(), HttpStatus.NOT_FOUND);
        }
        Wallet existingWallet = walletService.getWalletById(transactionRecurringDTO.getWallet().getWalletId());
        if (existingWallet == null) {
            return new ResponseEntity<>(
                    "Wallet not found with id: " + transactionRecurringDTO.getWallet().getWalletId(),
                    HttpStatus.NOT_FOUND);
        }

        Category existingCategory = categoryService.getByCateId(transactionRecurringDTO.getCategory().getId());
        if (existingCategory == null) {
            return new ResponseEntity<>("Category not found with id: " + transactionRecurringDTO.getCategory().getId(),
                    HttpStatus.NOT_FOUND);
        }

        Recurrence updateRecurrence = recurrenceConverter.convertToEntity(transactionRecurringDTO.getRecurrence());
        updateRecurrence.setStartDate(transactionRecurringDTO.getRecurrence().getStartDate());
        updateRecurrence.setUser(existingUser.get());
        Recurrence savedRecurrence = recurrenceService.saveRecurrence(updateRecurrence);
        TransactionRecurring transactionRecurring = existingTransactionRecurring.get();
        transactionRecurring.setRecurrence(savedRecurrence);
        transactionRecurring.setCategory(existingCategory);
        transactionRecurring.setUser(existingUser.get());
        transactionRecurring.setWallet(existingWallet);
        TransactionRecurring savedTransaction = transactionRecurringService
                .saveTransactionsRecurring(transactionRecurring);
        TransactionRecurringDTO savedTransactionDTO = modelMapper.map(savedTransaction, TransactionRecurringDTO.class);
        return ResponseEntity.ok(savedTransactionDTO);
    }

    @DeleteMapping("/delete/{transactionId}")
    public ResponseEntity<?> deleteTransaction(@PathVariable Integer transactionId) {
        TransactionRecurring transactionRecurring = transactionRecurringService
                .getTransactionsRecurringById(transactionId);
        if (transactionRecurring == null) {
            return ResponseEntity.notFound().build();
        }
        transactionRecurringService.deleteTransactionRecurring(transactionId);
        return ResponseEntity.noContent().build();
    }

}