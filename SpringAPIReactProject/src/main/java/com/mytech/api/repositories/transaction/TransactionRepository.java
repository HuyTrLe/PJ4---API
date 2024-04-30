package com.mytech.api.repositories.transaction;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mytech.api.models.transaction.Transaction;
import com.mytech.api.models.transaction.TransactionView;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

	@Query("SELECT t FROM Transaction t WHERE t.user.id = :userId")
	Page<Transaction> findByUserId(Integer userId, Pageable pageable);

	@Query("SELECT t FROM Transaction t WHERE t.user.id = :userId")
	List<Transaction> getByUserId(Integer userId);

	List<Transaction> getIncomeByUserIdAndCategoryType(int userId, Enum type);

	List<Transaction> getExpenseByUserIdAndCategoryType(int userId, Enum type);

	List<Transaction> getTotalAmountByUserIdAndWallet_WalletIdAndCategoryType(int userId, int walletId, Enum type);

	List<Transaction> getTotalExpenseByUserIdAndWallet_WalletIdAndCategoryType(int userId, int walletId, Enum type);

	List<Transaction> findByUserIdAndWallet_WalletId(int userId, Integer walletId);

	@Query("SELECT new com.mytech.api.models.transaction.TransactionView(c.name, t.amount, ci.path) " +
			"FROM Transaction t " +
			"JOIN t.category c " +
			"JOIN c.icon ci " +
			"WHERE t.user.id = :userId")
	Page<TransactionView> getTop5NewTransaction(int userId, Pageable pageable);

	@Query("SELECT NEW com.mytech.api.models.transaction.TransactionView(t.category.name, t.amount, c.icon.path) " +
			"FROM Transaction t " +
			"JOIN t.category c " +
			"WHERE t.user.id = :userId " +
			"ORDER BY t.amount DESC")
	Page<TransactionView> getTop5TransactionHightestMoney(int userId, Pageable pageable);

}
