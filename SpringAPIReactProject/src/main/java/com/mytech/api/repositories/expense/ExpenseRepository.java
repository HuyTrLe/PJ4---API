package com.mytech.api.repositories.expense;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mytech.api.models.expense.Expense;
import com.mytech.api.models.transaction.Transaction;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Integer> {
    List<Expense> findByUserId(int userId);

    List<Expense> findByUserIdAndExpenseDateBetween(int userId, Date startDate, Date endDate);

    List<Expense> findByCategoryId(int categoryId);
    
    Expense findByTransaction(Transaction transaction);
}
