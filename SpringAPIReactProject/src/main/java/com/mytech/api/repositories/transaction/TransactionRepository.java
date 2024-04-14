package com.mytech.api.repositories.transaction;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mytech.api.models.category.CateTypeENum;
import com.mytech.api.models.transaction.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

	@Query("SELECT t FROM Transaction t WHERE t.user.id = :userId")
	Page<Transaction> findByUserId(Integer userId, Pageable pageable);

	@Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId AND t.category.type = :categoryType")
	BigDecimal getTotalAmountByUserIdAndCategoryType(int userId, CateTypeENum categoryType);

	@Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId AND t.category.type = :expenseType")
	BigDecimal getTotalExpenseByUserId(int userId, CateTypeENum expenseType);
}
