package com.mytech.api.controllers.saving_goals;

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
import com.mytech.api.models.saving_goals.SavingGoal;
import com.mytech.api.models.saving_goals.SavingGoalDTO;
import com.mytech.api.services.saving_goals.SavingGoalsService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/savinggoals")
public class Saving_goalsController {
    private final SavingGoalsService savingGoalsService;
    private final ModelMapper modelMapper;

    public Saving_goalsController(SavingGoalsService savingGoalService, ModelMapper modelMapper) {
        this.savingGoalsService = savingGoalService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SavingGoalDTO>> getAllSavingGoalsByUser(@PathVariable Long userId) {
        List<SavingGoalDTO> savingGoals = savingGoalsService.getSavingGoalsByUserId(userId);
        if (savingGoals.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(savingGoals, HttpStatus.OK);
    }

    @GetMapping("/wallets/{walletId}/users/{userId}")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<List<SavingGoalDTO>> getAllSavingGoalByUserByWallet(@PathVariable int userId,
            @PathVariable Integer walletId) {
        List<SavingGoal> savingGoals = savingGoalsService.getSavingGoalsByWalletId(userId, walletId);
        List<SavingGoalDTO> savingGoalDTOs = savingGoals.stream()
                .map(savingGoal -> modelMapper.map(savingGoal, SavingGoalDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(savingGoalDTOs);

    }

    @DeleteMapping("/delete/{savingGoalId}")
    public ResponseEntity<String> deleteSavingGoal(@PathVariable Long savingGoalId, Authentication authentication) {
        if (savingGoalsService.existsSavingGoalById(savingGoalId)) {
            SavingGoalDTO savingGoal = savingGoalsService.getSavingGoalById(savingGoalId);
            savingGoalsService.deleteSavingGoalById(savingGoalId);
            MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
            if (!savingGoal.getUserId().equals(userDetails.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You are not authorized to delete this transaction.");
            }
            return ResponseEntity.ok("Saving goal deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Saving goal not found");
        }
    }

    @PostMapping("/create")
    @PreAuthorize("#savingGoalRequest.userId == authentication.principal.id")
    public ResponseEntity<?> createSavingGoal(@Valid @RequestBody SavingGoalDTO savingGoalRequest,
            BindingResult result) {
        if (result.hasErrors()) {
            String errors = result.getFieldErrors().stream().map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining("\n"));
            System.out.println(errors);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errors);
        }
        SavingGoalDTO createdSavingGoalDTO = savingGoalsService.createSavingGoal(savingGoalRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSavingGoalDTO);
    }

    @PutMapping("/update/{savingGoalId}")
    @PreAuthorize("#updatedSavingGoalDTO.userId == authentication.principal.id")
    public ResponseEntity<?> updateSavingGoal(@PathVariable Long savingGoalId,
            @RequestBody SavingGoalDTO updatedSavingGoalDTO) {
        try {
            SavingGoalDTO updatedSavingGoal = savingGoalsService.updateSavingGoal(savingGoalId, updatedSavingGoalDTO);
            return ResponseEntity.ok(updatedSavingGoal);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}