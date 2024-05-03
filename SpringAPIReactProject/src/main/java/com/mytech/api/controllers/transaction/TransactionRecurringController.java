package com.mytech.api.controllers.transaction;

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
import com.mytech.api.models.recurrence.RecurrenceConverter;
import com.mytech.api.models.transaction.TransactionRecurring;
import com.mytech.api.models.transaction.TransactionRecurringDTO;
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

    @PreAuthorize("#transactionRecurringDTO.userId == authentication.principal.id")
    @PostMapping("/create")
    public ResponseEntity<?> createTransaction(@RequestBody @Valid TransactionRecurringDTO transactionRecurringDTO,
            BindingResult result) {
        if (result.hasErrors()) {
            String errors = result.getFieldErrors().stream().map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            System.out.println(errors);
            return ResponseEntity.badRequest().body(errors);
        }
        return transactionRecurringService.createTransaction(transactionRecurringDTO);
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

    @PreAuthorize("#transactionRecurringDTO.userId == authentication.principal.id")
    @PutMapping("/update/{transactionId}")
    public ResponseEntity<?> updateTransaction(@PathVariable Integer transactionId,
            @RequestBody @Valid TransactionRecurringDTO transactionRecurringDTO, BindingResult result) {
        if (result.hasErrors()) {
            String errors = result.getFieldErrors().stream().map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            System.out.println(errors);
            return ResponseEntity.badRequest().body(errors);
        }
        return transactionRecurringService.updateTransaction(transactionId, transactionRecurringDTO);
    }

    @DeleteMapping("/delete/{transactionId}")
    public ResponseEntity<?> deleteTransaction(@PathVariable Integer transactionId, Authentication authentication) {
        return transactionRecurringService.deleteTransaction(transactionId, authentication);
    }

}