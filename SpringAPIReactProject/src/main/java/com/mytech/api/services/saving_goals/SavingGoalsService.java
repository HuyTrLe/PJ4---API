package com.mytech.api.services.saving_goals;

import java.util.List;

import com.mytech.api.models.saving_goals.SavingGoal;
import com.mytech.api.models.saving_goals.SavingGoalDTO;

public interface SavingGoalsService {

    List<SavingGoalDTO> getAllSavingGoals();

    List<SavingGoalDTO> getSavingGoalsByUserId(Long userId);

    SavingGoalDTO getSavingGoalById(Long savingGoalId);

    SavingGoalDTO createSavingGoal(SavingGoalDTO savingGoalRequest);

    SavingGoalDTO updateSavingGoal(Long savingGoalId, SavingGoalDTO updatedSavingGoalDTO);

    void deleteSavingGoalById(Long savingGoalId);

    boolean existsSavingGoalById(Long savingGoalId);

    List<SavingGoal> getSavingGoalsByWalletId(int userId, Integer walletId);
}
