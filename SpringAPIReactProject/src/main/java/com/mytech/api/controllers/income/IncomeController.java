package com.mytech.api.controllers.income;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/create")
    @PreAuthorize("#incomeDTO.userId == authentication.principal.id")
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

    @GetMapping("/{incomeId}")
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
                .map(income -> modelMapper.map(income, IncomeDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(incomeDTOs);
    }

    @PutMapping("/update/{incomeId}")
    @PreAuthorize("#incomeDTO.userId == authentication.principal.id")
    public ResponseEntity<?> updateIncome(@PathVariable int incomeId, @RequestBody @Valid IncomeDTO incomeDTO,
            BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getFieldErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", ")));
        }
        Income updatedIncome = modelMapper.map(incomeDTO, Income.class);
        updatedIncome = incomeService.saveIncome(updatedIncome);
        IncomeDTO updatedIncomeDTO = modelMapper.map(updatedIncome, IncomeDTO.class);
        return ResponseEntity.ok(updatedIncomeDTO);
    }

    @DeleteMapping("/delete/{incomeId}")
    public ResponseEntity<?> deleteIncome(@PathVariable int incomeId) {
        Income existingIncome = incomeService.getIncomeById(incomeId);
        if (existingIncome == null) {
            return ResponseEntity.notFound().build();
        }
        incomeService.deleteIncome(incomeId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
