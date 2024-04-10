package com.mytech.api.repositories.inome;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mytech.api.models.income.Income;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Integer>{
	List<Income> findByUserId(int userId);

    List<Income> findByUserIdAndIncomeDateBetween(int userId, Date startDate, Date endDate);

    List<Income> findByCategoryId(int categoryId);
}
