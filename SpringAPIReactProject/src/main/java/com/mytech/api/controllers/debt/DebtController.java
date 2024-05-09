package com.mytech.api.controllers.debt;

import java.util.List;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.mytech.api.models.bill.Bill;
import com.mytech.api.models.bill.BillDTO;
import com.mytech.api.models.bill.BillResponse;
import com.mytech.api.models.debt.Debt;
import com.mytech.api.models.debt.DebtDTO;
import com.mytech.api.services.debt.DebtService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/debts")
public class DebtController {
	@Autowired
	ModelMapper modelMapper;
	
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
            return ResponseEntity.ok(debtDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/delete/{debtId}")
    public ResponseEntity<String> deleteDebt(@PathVariable Long debtId, Authentication authentication) {
        if (debtService.existsDebtById(debtId)) {
            debtService.deleteDebtById(debtId);
            DebtDTO debtDTO = debtService.getDebtById(debtId);
            MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
            if (!debtDTO.getUserId().equals(userDetails.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You are not authorized to delete this transaction.");
            }
            return ResponseEntity.ok("Debt deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Debt not found");
        }
    }

    @PostMapping("/create")
    @PreAuthorize("#debtRequest.userId == authentication.principal.id")
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
    @PreAuthorize("#updatedDebtDTO.userId == authentication.principal.id")
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
    
    @GetMapping("/findDebtActive/user/{userId}")
    public ResponseEntity<?> findDebtActive(@PathVariable Long userId) {  	
        List<Debt> debts = debtService.findDebtActive(userId);
        if (debts.isEmpty()) {
            return new ResponseEntity<>(debts, HttpStatus.NOT_FOUND);
        }
        List<DebtDTO> debtDTOs = debts.stream()
                .map(debt -> modelMapper.map(debt, DebtDTO.class))
                .collect(Collectors.toList());
		return ResponseEntity.ok(debtDTOs);
    }
    
    
    @GetMapping("/findDebtPaid/user/{userId}")
    public ResponseEntity<?> findDebtPaid(@PathVariable Long userId) {
    	 List<Debt> debts = debtService.findDebtPaid(userId);
         if (debts.isEmpty()) {
             return new ResponseEntity<>(debts, HttpStatus.NOT_FOUND);
         }
         List<DebtDTO> debtDTOs = debts.stream()
                 .map(debt -> modelMapper.map(debt, DebtDTO.class))
                 .collect(Collectors.toList());
 		return ResponseEntity.ok(debtDTOs);
    }
    
    @GetMapping("/findDebt/user/{userId}")
    public ResponseEntity<?> findDebt(@PathVariable Long userId) {
    	 List<Debt> debts = debtService.findDebt(userId);
         if (debts.isEmpty()) {
             return new ResponseEntity<>(debts, HttpStatus.NOT_FOUND);
         }
         List<DebtDTO> debtDTOs = debts.stream()
                 .map(debt -> modelMapper.map(debt, DebtDTO.class))
                 .collect(Collectors.toList());
 		return ResponseEntity.ok(debtDTOs);
    }
    
    @GetMapping("/findLoan/user/{userId}")
    public ResponseEntity<?> findLoan(@PathVariable Long userId) {
    	System.out.println(userId);
    	 List<Debt> debts = debtService.findLoan(userId);
         if (debts.isEmpty()) {
             return new ResponseEntity<>(debts, HttpStatus.NOT_FOUND);
         }
         List<DebtDTO> debtDTOs = debts.stream()
                 .map(debt -> modelMapper.map(debt, DebtDTO.class))
                 .collect(Collectors.toList());
 		return ResponseEntity.ok(debtDTOs);
    }
}