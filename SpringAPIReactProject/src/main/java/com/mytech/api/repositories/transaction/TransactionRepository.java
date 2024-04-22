package com.mytech.api.repositories.transaction;

import java.math.BigDecimal;
import java.util.List;

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

	@Query("SELECT t FROM Transaction t WHERE t.user.id = :userId")
	List<Transaction> getByUserId(Integer userId);

	@Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId AND t.category.type = :categoryType")
	BigDecimal getTotalAmountByUserIdAndCategoryType(int userId, CateTypeENum categoryType);

	@Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId AND t.category.type = :expenseType")
	BigDecimal getTotalExpenseByUserId(int userId, CateTypeENum expenseType);

	@Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId AND t.wallet.id = :walletId AND t.category.type = :categoryType")
	BigDecimal getTotalAmountByUserIdAndWalletId(int userId, int walletId, CateTypeENum categoryType);

	@Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId AND t.wallet.id = :walletId AND t.category.type = :expenseType")
	BigDecimal getTotalExpenseByUserIdAndWalletId(int userId, int walletId, CateTypeENum expenseType);

	List<Transaction> findByUserIdAndWallet_WalletId(int userId, Integer walletId);

}
