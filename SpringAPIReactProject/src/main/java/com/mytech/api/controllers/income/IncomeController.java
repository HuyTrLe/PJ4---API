package com.mytech.api.controllers.income;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;

import com.mytech.api.models.expense.ExpenseDTO;
import com.mytech.api.models.income.Income;
import com.mytech.api.models.income.IncomeDTO;
import com.mytech.api.services.income.IncomeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/incomes")
public class IncomeController {

    private final IncomeService incomeService;
    private final ModelMapper modelMapper;

    public IncomeController(IncomeService incomeService, ModelMapper modelMapper) {
        this.incomeService = incomeService;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    public ResponseEntity<?> createIncome(@RequestBody @Valid IncomeDTO incomeDTO, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getFieldErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", ")));
        }
        Income income = modelMapper.map(incomeDTO, Income.class);
        Income savedIncome = incomeService.saveIncome(income);
        IncomeDTO savedIncomeDTO = modelMapper.map(savedIncome, IncomeDTO.class);
        return ResponseEntity.ok(savedIncomeDTO); 
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<?> getIncomeById(@PathVariable int incomeId) {
        Income income = incomeService.getIncomeById(incomeId);
        if (income != null) {
            IncomeDTO incomeDTO = modelMapper.map(income, IncomeDTO.class);
            return ResponseEntity.ok(incomeDTO);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<IncomeDTO>> getIncomesByUserId(@PathVariable int userId) {
        List<Income> incomes = incomeService.getIncomesByUserId(userId);
        List<IncomeDTO> incomeDTOs = incomes.stream()
                .map(expense -> modelMapper.map(expense, IncomeDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(incomeDTOs);
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<?> updateIncome(@PathVariable int incomeId, @RequestBody @Valid IncomeDTO incomeDTO, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getFieldErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", ")));
        }
        Income updatedIncome = modelMapper.map(incomeDTO, Income.class);
        updatedIncome = incomeService.saveIncome(updatedIncome);
        ExpenseDTO updatedExpenseDTO = modelMapper.map(updatedIncome, ExpenseDTO.class);
        return ResponseEntity.ok(updatedExpenseDTO);
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<?> deleteIncome(@PathVariable int incomeId) {
        Income existingExpense = incomeService.getIncomeById(incomeId);
        if (existingExpense == null) {
            return ResponseEntity.notFound().build();
        }
        incomeService.deleteIncome(incomeId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
