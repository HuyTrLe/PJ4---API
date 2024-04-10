package com.mytech.api.services.income;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mytech.api.models.income.Income;
import com.mytech.api.repositories.income.IncomeRepository;

@Service
public class IncomeServiceImpl implements IncomeService{
	
	private final IncomeRepository incomeRepository;
	
	public IncomeServiceImpl(IncomeRepository incomeRepository) {
		this.incomeRepository = incomeRepository;
	}

	@Override
	public Income saveIncome(Income income) {
		return incomeRepository.save(income);
	}

	@Override
	public Income getIncomeById(int incomeId) {
		return incomeRepository.findById(incomeId).orElse(null);
	}

	@Override
	public List<Income> getIncomesByUserId(int userId) {
		return incomeRepository.findByUserId(userId);
	}

	@Override
	public void deleteIncome(int incomeId) {
		if (incomeRepository.existsById(incomeId)) {
            incomeRepository.deleteById(incomeId);
        } else {
            throw new RuntimeException("Expense not found with id: " + incomeId);
        }
	}

}
