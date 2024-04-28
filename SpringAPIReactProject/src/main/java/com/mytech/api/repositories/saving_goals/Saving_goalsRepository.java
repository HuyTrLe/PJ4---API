package com.mytech.api.repositories.saving_goals;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mytech.api.models.saving_goals.SavingGoal;

public interface Saving_goalsRepository extends JpaRepository<SavingGoal, Long> {

	@Query("SELECT s FROM SavingGoal s WHERE s.user.id = :userId")
	List<SavingGoal> findByUserId(Long userId);

	// @Query("SELECT COUNT(s) > 0 FROM SavingGoal s WHERE s.id = :savingGoalId")
	// boolean existsById(@Param("savingGoalId") Long savingGoalId);

	void deleteSavingGoalById(Long savingGoalId);

}
