package com.mytech.api.controllers.budget;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
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
import org.springframework.web.bind.annotation.RestController;

import com.mytech.api.auth.services.MyUserDetails;
import com.mytech.api.models.budget.Budget;
import com.mytech.api.models.budget.BudgetDTO;
import com.mytech.api.models.budget.BudgetResponse;
import com.mytech.api.models.budget.ParamBudget;
import com.mytech.api.services.budget.BudgetService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;
    private final ModelMapper modelMapper;

    public BudgetController(BudgetService budgetService, ModelMapper modelMapper) {
        this.budgetService = budgetService;
        this.modelMapper = modelMapper;
    }

    @PostMapping("/create")
    @PreAuthorize("#budgetDTO.userId == authentication.principal.id")
    public ResponseEntity<?> createBudget(@RequestBody @Valid BudgetDTO budgetDTO, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getFieldErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", ")));
        }
        Budget budget = modelMapper.map(budgetDTO, Budget.class);
        Budget savedBudget = budgetService.saveBudget(budget);
        BudgetDTO savedBudgetDTO = modelMapper.map(savedBudget, BudgetDTO.class);
        return ResponseEntity.ok(savedBudgetDTO);
    }

    @GetMapping("/{budgetId}")
    public ResponseEntity<?> getBudgetById(@PathVariable int budgetId) {
        Budget budget = budgetService.getBudgetById(budgetId);
        if (budget != null) {
            BudgetDTO budgetDTO = modelMapper.map(budget, BudgetDTO.class);
            return ResponseEntity.ok(budgetDTO);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<BudgetDTO>> getBudgetsByUserId(@PathVariable int userId) {
        List<Budget> budgets = budgetService.getBudgetsByUserId(userId);
        List<BudgetDTO> budgetDTOs = budgets.stream()
                .map(budget -> modelMapper.map(budget, BudgetDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(budgetDTOs);
    }
    
    @PostMapping("/getBudgetWithTime")
 
    public ResponseEntity<List<BudgetResponse>> getBudgetWithTime(@RequestBody @Valid ParamBudget paramPudget, BindingResult result) {
        List<BudgetResponse> budgets = budgetService.getBudgetWithTime(paramPudget);
        return ResponseEntity.ok(budgets);
    }

    @PutMapping("/update/{budgetId}")
    @PreAuthorize("#budgetDTO.userId == authentication.principal.id")
    public ResponseEntity<?> updateBudget(@PathVariable int budgetId, @RequestBody @Valid BudgetDTO budgetDTO,
            BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getFieldErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", ")));
        }
        Budget updatedBudget = modelMapper.map(budgetDTO, Budget.class);
        updatedBudget = budgetService.saveBudget(updatedBudget);
        BudgetDTO updatedBudgetDTO = modelMapper.map(updatedBudget, BudgetDTO.class);
        return ResponseEntity.ok(updatedBudgetDTO);
    }

    @DeleteMapping("/delete/{budgetId}")
    public ResponseEntity<?> deleteBudget(@PathVariable int budgetId, Authentication authentication) {
        Budget existingBudget = budgetService.getBudgetById(budgetId);
        if (existingBudget == null) {
            return ResponseEntity.notFound().build();
        }
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        if (!existingBudget.getUser().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not authorized to delete this transaction.");
        }
        budgetService.deleteBudget(budgetId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
    
    @GetMapping("valid/user/{userId}")
    public ResponseEntity<List<BudgetDTO>> getValidBudgetsByUserId(@PathVariable int userId) {
        List<Budget> validBudgets = budgetService.getValidBudget(userId);
        List<BudgetDTO> validBudgetDTOs = validBudgets.stream()
                .map(budget -> modelMapper.map(budget, BudgetDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(validBudgetDTOs);
    }
    
    @GetMapping("past/user/{userId}")
    public ResponseEntity<List<BudgetDTO>> getPastBudgets(@PathVariable int userId) {
        List<Budget> validBudgets = budgetService.getPastBudgets(userId);
        List<BudgetDTO> validBudgetDTOs = validBudgets.stream()
                .map(budget -> modelMapper.map(budget, BudgetDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(validBudgetDTOs);
    }
    
    @GetMapping("future/user/{userId}")
    public ResponseEntity<List<BudgetDTO>> getFutureBudgets(@PathVariable int userId) {
        List<Budget> validBudgets = budgetService.getFutureBudgets(userId);
        List<BudgetDTO> validBudgetDTOs = validBudgets.stream()
                .map(budget -> modelMapper.map(budget, BudgetDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(validBudgetDTOs);
    }
}
