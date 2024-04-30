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
	
	@Query("SELECT c.name,ci.path,t.amount \n"
			+ "FROM Transaction t\n"
			+ "JOIN t.category c\n"
			+ "JOIN c.icon ci where t.user.id = :userId")
	Page<TransactionView> getTop5NewTransaction(int userId,Pageable pageable);
	@Query("SELECT t FROM Transaction t where t.user.id = :userId order by amount  desc ")
	Page<TransactionView> getTop5TransactionHightestMoney(int userId,Pageable pageable);

}


