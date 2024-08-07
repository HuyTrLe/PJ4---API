package com.mytech.api.repositories.saving_goals;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mytech.api.models.saving_goals.SavingGoal;

public interface Saving_goalsRepository extends JpaRepository<SavingGoal, Long> {

	@Query("SELECT s FROM SavingGoal s WHERE s.user.id = :userId")
	List<SavingGoal> findByUserId(Long userId);

	// @Query("SELECT COUNT(s) > 0 FROM SavingGoal s WHERE s.id = :savingGoalId")
	// boolean existsById(@Param("savingGoalId") Long savingGoalId);

	void deleteSavingGoalById(Long savingGoalId);

	List<SavingGoal> findByUserIdAndWallet_WalletId(int userId, Integer walletId);

	@Query("SELECT s FROM SavingGoal s WHERE s.user.id = :userId and s.targetAmount > currentAmount")
	List<SavingGoal> findWorkingByUserId(Long userId);

	@Query("SELECT s FROM SavingGoal s WHERE s.user.id = :userId and s.targetAmount <= currentAmount")
	List<SavingGoal> findFinishedByUserId(Long userId);

	@Query("SELECT s FROM SavingGoal s WHERE s.user.id = :userId and s.id = :savingId")
	List<SavingGoal> getSavingWithSavingID(int userId, Long savingId);

	List<SavingGoal> findByWallet_WalletId(Integer walletId);

	Page<SavingGoal> findByUserId(int userId, Pageable pageable);

}
