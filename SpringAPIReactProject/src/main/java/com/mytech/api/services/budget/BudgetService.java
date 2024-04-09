package com.mytech.api.services.budget;

import java.util.List;

import com.mytech.api.models.budget.Budget;


public interface BudgetService {

	Budget saveBudget(Budget budget);

	Budget getBudgetById(int budgetId);

	List<Budget> getBudgetsByUserId(int userId);

	void deleteBudget(int budgetId);
}
