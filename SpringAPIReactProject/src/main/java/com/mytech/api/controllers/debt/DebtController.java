package com.mytech.api.controllers.debt;

import java.util.List;
import java.util.stream.Collectors;

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
import org.springframework.web.bind.annotation.RestController;

import com.mytech.api.models.debt.DebtDTO;
import com.mytech.api.services.debt.DebtService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/debts")
public class DebtController {
    private final DebtService debtService;

    public DebtController(DebtService debtService) {
        this.debtService = debtService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DebtDTO>> getAllDebtsByUser(@PathVariable Long userId) {
        List<DebtDTO> debts = debtService.getDebtsByUserId(userId);
        if (debts.isEmpty()) {
            return new ResponseEntity<>(debts, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(debts, HttpStatus.OK);
    }
    
    @GetMapping("/{debtId}")
    public ResponseEntity<DebtDTO> getDebtById(@PathVariable Long debtId) {
        try {
            DebtDTO debtDTO = debtService.getDebtById(debtId);
            return ResponseEntity.ok(debtDTO); // Return the found DebtDTO with an OK status
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Return a NOT FOUND status if the debt is not found
        } catch (Exception e) {
            System.out.println(e); // Consider using a logger instead of System.out.println
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Return an INTERNAL SERVER ERROR status for any other exceptions
        }
    }
    
    @DeleteMapping("/delete/{debtId}")
    public ResponseEntity<String> deleteDebt(@PathVariable Long debtId) {
        if (debtService.existsDebtById(debtId)) {
            debtService.deleteDebtById(debtId);
            return ResponseEntity.ok("Debt deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Debt not found");
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createDebt(@Valid @RequestBody DebtDTO debtRequest, BindingResult result) {
        if (result.hasErrors()) {
            String errors = result.getFieldErrors().stream().map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining("\n"));
            System.out.println(errors);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errors);
        }
        DebtDTO createdDebtDTO = debtService.createDebt(debtRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDebtDTO);
    }

    @PutMapping("/update/{debtId}")
    public ResponseEntity<DebtDTO> updateDebt(@PathVariable Long debtId, @RequestBody DebtDTO updatedDebtDTO) {
        try {
            DebtDTO updatedDebt = debtService.updateDebt(debtId, updatedDebtDTO);
            return ResponseEntity.ok(updatedDebt);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}