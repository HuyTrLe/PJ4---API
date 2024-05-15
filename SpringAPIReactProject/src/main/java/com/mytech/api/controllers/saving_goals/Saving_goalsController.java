package com.mytech.api.controllers.saving_goals;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

import com.mytech.api.auth.services.MyUserDetails;
import com.mytech.api.models.debt.DebtDTO;
import com.mytech.api.models.saving_goals.SavingGoal;
import com.mytech.api.models.saving_goals.SavingGoalDTO;
import com.mytech.api.models.saving_goals.TransactionWithSaving;
import com.mytech.api.models.transaction.TransactionData;
import com.mytech.api.models.wallet.Wallet;
import com.mytech.api.models.wallet.WalletDTO;
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
        LocalDate currentDate = LocalDate.now();

        List<SavingGoal> savingGoals = savingGoalsService.getSavingGoalsByWalletId(userId, walletId);

        // Lọc danh sách các mục tiêu tiết kiệm để chỉ bao gồm các mục tiêu bắt đầu từ
        // hôm nay hoặc trước đó, và không có ngày kết thúc trong tương lai
        List<SavingGoal> filteredSavingGoals = savingGoals.stream()
                .filter(savingGoal -> (savingGoal.getStartDate().isEqual(currentDate)
                        || savingGoal.getStartDate().isBefore(currentDate)) // Start date là trước đó hoặc bằng ngày
                                                                            // hiện tại
                        && (savingGoal.getEndDate() == null || savingGoal.getEndDate().isEqual(currentDate)
                                || savingGoal.getEndDate().isAfter(currentDate))) // End date vẫn còn hiệu lực hoặc là
                                                                                  // forever
                .collect(Collectors.toList());

        List<SavingGoalDTO> savingGoalDTOs = filteredSavingGoals.stream()
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }
        SavingGoalDTO createdSavingGoalDTO = savingGoalsService.createSavingGoal(savingGoalRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSavingGoalDTO);
    }

    @PutMapping("/update/{savingGoalId}")
    @PreAuthorize("#updatedSavingGoalDTO.userId == authentication.principal.id")
    public ResponseEntity<?> updateSavingGoal(@PathVariable Long savingGoalId,
            @Valid @RequestBody SavingGoalDTO updatedSavingGoalDTO, BindingResult result) {
        if (result.hasErrors()) {
            String errors = result.getFieldErrors().stream().map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining("\n"));
            System.out.println(errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }
        SavingGoalDTO updatedSavingGoal = savingGoalsService.updateSavingGoal(savingGoalId, updatedSavingGoalDTO);
        return ResponseEntity.ok(updatedSavingGoal);
    }

    @GetMapping("findWorkingByUserId/user/{userId}")
    public ResponseEntity<List<SavingGoalDTO>> findWorkingByUserId(@PathVariable Long userId) {
        List<SavingGoal> savingGoals = savingGoalsService.findWorkingByUserId(userId);
        if (savingGoals.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        List<SavingGoalDTO> savingDTOs = savingGoals.stream()
                .map(saving -> modelMapper.map(saving, SavingGoalDTO.class))
                .collect(Collectors.toList());
        return new ResponseEntity<>(savingDTOs, HttpStatus.OK);
    }

    @GetMapping("findFinishedByUserId/user/{userId}")
    public ResponseEntity<List<SavingGoalDTO>> findFinishedByUserId(@PathVariable Long userId) {
        List<SavingGoal> savingGoals = savingGoalsService.findFinishedByUserId(userId);
        if (savingGoals.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        List<SavingGoalDTO> savingDTOs = savingGoals.stream()
                .map(saving -> modelMapper.map(saving, SavingGoalDTO.class))
                .collect(Collectors.toList());
        return new ResponseEntity<>(savingDTOs, HttpStatus.OK);
    }

    @PostMapping("/getSavingWithSavingID")
    public ResponseEntity<?> GetSavingWithSavingID(@RequestBody @Valid TransactionWithSaving param) {
        List<SavingGoal> savingData = savingGoalsService.getSavingWithSavingID(param);
        if (savingData != null && !savingData.isEmpty()) {
            List<SavingGoalDTO> savingDTOs = savingData.stream()
                    .map(saving -> modelMapper.map(saving, SavingGoalDTO.class))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(savingDTOs);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/page/users/{userId}")
    public ResponseEntity<Page<SavingGoalDTO>> getPageAllGoals(@PathVariable int userId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SavingGoal> goals = savingGoalsService.getPageAllGoals(userId, pageable);
        Page<SavingGoalDTO> savingGoalDTOs = goals.map(goal -> modelMapper.map(goal, SavingGoalDTO.class));
        return ResponseEntity.ok(savingGoalDTOs);
    }
}