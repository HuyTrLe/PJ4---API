package com.mytech.api.controllers.expense;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;

import com.mytech.api.models.expense.Expense;
import com.mytech.api.models.expense.ExpenseDTO;
import com.mytech.api.services.expense.ExpenseService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final ModelMapper modelMapper;

    public ExpenseController(ExpenseService expenseService, ModelMapper modelMapper) {
        this.expenseService = expenseService;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    public ResponseEntity<?> createExpense(@RequestBody @Valid ExpenseDTO expenseDTO, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getFieldErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", ")));
        }
        Expense expense = modelMapper.map(expenseDTO, Expense.class);
        Expense savedExpense = expenseService.saveExpense(expense);
        ExpenseDTO savedExpenseDTO = modelMapper.map(savedExpense, ExpenseDTO.class);
        return ResponseEntity.ok(savedExpenseDTO); // You might want to return ResponseEntity.created()
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<?> getExpenseById(@PathVariable int expenseId) {
        Expense expense = expenseService.getExpenseById(expenseId);
        if (expense != null) {
            ExpenseDTO expenseDTO = modelMapper.map(expense, ExpenseDTO.class);
            return ResponseEntity.ok(expenseDTO);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<ExpenseDTO>> getExpensesByUserId(@PathVariable int userId) {
        List<Expense> expenses = expenseService.getExpensesByUserId(userId);
        List<ExpenseDTO> expenseDTOs = expenses.stream()
                .map(expense -> modelMapper.map(expense, ExpenseDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(expenseDTOs);
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<?> updateExpense(@PathVariable int expenseId, @RequestBody @Valid ExpenseDTO expenseDTO, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getFieldErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", ")));
        }
        Expense updatedExpense = modelMapper.map(expenseDTO, Expense.class);
        updatedExpense = expenseService.saveExpense(updatedExpense);
        ExpenseDTO updatedExpenseDTO = modelMapper.map(updatedExpense, ExpenseDTO.class);
        return ResponseEntity.ok(updatedExpenseDTO);
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<?> deleteExpense(@PathVariable int expenseId) {
        Expense existingExpense = expenseService.getExpenseById(expenseId);
        if (existingExpense == null) {
            return ResponseEntity.notFound().build();
        }
        expenseService.deleteExpense(expenseId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}