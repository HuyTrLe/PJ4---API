package com.mytech.api.services.saving_goals;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mytech.api.models.saving_goals.SavingGoal;
import com.mytech.api.models.saving_goals.SavingGoalDTO;
import com.mytech.api.models.saving_goals.TransactionWithSaving;
import com.mytech.api.models.wallet.Wallet;

public interface SavingGoalsService {

    List<SavingGoalDTO> getAllSavingGoals();

    List<SavingGoalDTO> getSavingGoalsByUserId(Long userId);

    SavingGoalDTO getSavingGoalById(Long savingGoalId);

    SavingGoalDTO createSavingGoal(SavingGoalDTO savingGoalRequest);

    SavingGoalDTO updateSavingGoal(Long savingGoalId, SavingGoalDTO updatedSavingGoalDTO);

    void deleteSavingGoalById(Long savingGoalId);

    boolean existsSavingGoalById(Long savingGoalId);

    List<SavingGoal> getSavingGoalsByWalletId(int userId, Integer walletId);

    List<SavingGoal> findFinishedByUserId(Long userId);

    List<SavingGoal> findWorkingByUserId(Long userId);

    List<SavingGoal> getSavingWithSavingID(TransactionWithSaving param);

    void checkAndSendSavingGoalProgressNotifications(SavingGoal savingGoal);

    Page<SavingGoal> getPageAllGoals(int userId, Pageable pageable);

}
