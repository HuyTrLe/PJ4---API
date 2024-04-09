package com.mytech.api.services.income;

import java.util.List;
import com.mytech.api.models.income.Income;

public interface IncomeService {
	Income saveIncome(Income income);

	Income getIncomeById(int incomeId);

	List<Income> getIncomesByUserId(int userId);

	void deleteIncome(int incomeId);
}
