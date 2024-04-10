package com.mytech.api.controllers.transaction;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.mytech.api.models.transaction.Transaction;
import com.mytech.api.models.transaction.TransactionDTO;
import com.mytech.api.services.transaction.TransactionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final ModelMapper modelMapper;

    public TransactionController(TransactionService transactionService, ModelMapper modelMapper) {
        this.transactionService = transactionService;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    public ResponseEntity<?> createTransaction(@RequestBody @Valid TransactionDTO transactionDTO, BindingResult result) {
        if (result.hasErrors()) {
            String errors = result.getFieldErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body(errors);
        }
        Transaction transaction = modelMapper.map(transactionDTO, Transaction.class);
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
   
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByUserId(@PathVariable Integer userId) {
        List<Transaction> transactions = transactionService.getAllTransactionsByUserId(userId);
        List<TransactionDTO> transactionsDTO = transactions.stream()
                .map(transaction -> modelMapper.map(transaction, TransactionDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactionsDTO);
    }
   
    @PutMapping("/{transactionId}")
    public ResponseEntity<?> updateTransaction(@PathVariable Integer transactionId,
                                               @RequestBody @Valid TransactionDTO transactionDTO,
                                               BindingResult result) {
        if (result.hasErrors()) {
            String errors = result.getFieldErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body(errors);
        }
        Transaction updatedTransaction = modelMapper.map(transactionDTO, Transaction.class);
        updatedTransaction.setTransactionId(transactionId);
        updatedTransaction = transactionService.saveTransaction(updatedTransaction);
        TransactionDTO updatedTransactionDTO = modelMapper.map(updatedTransaction, TransactionDTO.class);
        return ResponseEntity.ok(updatedTransactionDTO);
    }
   
    @DeleteMapping("/{transactionId}")
    public ResponseEntity<?> deleteTransaction(@PathVariable Integer transactionId) {
        transactionService.deleteTransaction(transactionId);
        return ResponseEntity.noContent().build();
    }
}