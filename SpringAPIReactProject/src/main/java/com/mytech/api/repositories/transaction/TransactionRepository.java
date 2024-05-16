package com.mytech.api.repositories.transaction;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mytech.api.models.category.CateTypeENum;
import com.mytech.api.models.transaction.Transaction;
import com.mytech.api.models.transaction.TransactionData;
import com.mytech.api.models.transaction.TransactionReport;
import com.mytech.api.models.transaction.TransactionSavingGoalsView;
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

	List<Transaction> findByCategory_Id(Long categoryId);

	List<Transaction> findBySavingGoal_Id(Long savingGoalId);

	@Query("SELECT new com.mytech.api.models.transaction.TransactionView(c.name, t.amount, ci.path, t.transactionDate) "
			+ "FROM Transaction t " + "JOIN t.category c " + "JOIN c.icon ci "
			+ "WHERE t.user.id = :userId order by t.id desc")
	Page<TransactionView> getTop5NewTransaction(int userId, Pageable pageable);

	@Query("SELECT NEW com.mytech.api.models.transaction.TransactionView(t.category.name, t.amount, c.icon.path, t.transactionDate) "
			+
			"FROM Transaction t " +
			"JOIN t.category c " +
			"WHERE t.user.id = :userId  and c.type = com.mytech.api.models.category.CateTypeENum.EXPENSE and t.transactionDate between :fromDate and :toDate "
			+
			"ORDER BY t.amount DESC")
	Page<TransactionView> getTop5TransactionHightestMoney(int userId, LocalDate fromDate, LocalDate toDate,
			Pageable pageable);

	@Query("SELECT new com.mytech.api.models.transaction.TransactionReport(" +
			"t.transactionDate, " +
			"SUM(t.amount)) " +
			"FROM Transaction t " +
			"JOIN t.category c " +
			"JOIN c.icon ci " +
			"WHERE t.user.id = :userId and t.transactionDate between :fromDate and :toDate and c.type = com.mytech.api.models.category.CateTypeENum.EXPENSE " +
			"GROUP BY t.transactionDate " +
			"ORDER BY t.transactionDate ASC")
	List<TransactionReport> getTransactionReport(int userId, LocalDate fromDate, LocalDate toDate);

	@Query("SELECT new com.mytech.api.models.transaction.TransactionReport(" +
	// "FUNCTION('DATE_FORMAT', t.transactionDate, '%Y-%m'), " +
			"t.transactionDate, " +
			"SUM(t.amount)) " +
			"FROM Transaction t " +
			"WHERE t.user.id = :userId AND " +
			"(t.transactionDate BETWEEN :fromDate AND :toDate OR " +
			"t.transactionDate BETWEEN :prevMonthStart AND :prevMonthEnd) " +
			// "GROUP BY FUNCTION('DATE_FORMAT', t.transactionDate, '%Y-%m') " +
			"GROUP BY t.transactionDate " +
			"ORDER BY transactionDate ASC")
	List<TransactionReport> getTransactionReportMonth(int userId, LocalDate fromDate, LocalDate toDate,
			LocalDate prevMonthStart, LocalDate prevMonthEnd);

	@Query("SELECT new com.mytech.api.models.transaction.TransactionData(" +
			"t.transactionId,c.name, ci.path, t.amount, c.type, " +
			"(SELECT SUM(tx.amount) FROM Transaction tx " +
			"JOIN tx.category tc " +
			"WHERE tc.type = c.type AND tx.user.id = :userId " +
			"GROUP BY tc.type),c.id,t.transactionDate ) " +
			"FROM Transaction t " +
			"JOIN t.category c " +
			"JOIN c.icon ci " +
			"WHERE t.user.id = :userId and t.transactionDate between :fromDate and :toDate and c.type = :type OR ( :walletId != 0 and t.wallet.id = :walletId) "
			+
			"ORDER BY t.id DESC")
	List<TransactionData> FindTransaction(int userId, LocalDate fromDate, LocalDate toDate, CateTypeENum type,
			int walletId);

	@Query("SELECT NEW com.mytech.api.models.transaction.TransactionView(c.name, t.amount, c.icon.path, t.transactionDate) "
			+ "FROM Transaction t " + "JOIN t.category c "
			+ "WHERE t.user.id = :userId and c.type = com.mytech.api.models.category.CateTypeENum.EXPENSE "
			+ "ORDER BY t.amount DESC")
	Page<TransactionView> getTop5TransactionHightestMoney(int userId, Pageable pageable);

	@Query("SELECT new com.mytech.api.models.transaction.TransactionData("
			+ "t.transactionId,c.name, ci.path, t.amount, c.type, "
			+ "(SELECT SUM(tx.amount) FROM Transaction tx " + "JOIN tx.category tc "
			+ "WHERE tc.type = c.type AND tx.user.id = :userId " + "GROUP BY tc.type),c.id,t.transactionDate ) "
			+ "FROM Transaction t "
			+ "JOIN t.category c " + "JOIN c.icon ci "
			+ "WHERE t.user.id = :userId and t.transactionDate between :fromDate and :toDate " + "ORDER BY t.id DESC")
	List<TransactionData> getTransactionWithTime(int userId, LocalDate fromDate, LocalDate toDate);

	@Query("SELECT new com.mytech.api.models.transaction.TransactionData("
			+ "t.transactionId,c.name, ci.path, t.amount, c.type, "
			+ "(SELECT SUM(tx.amount) FROM Transaction tx " + "JOIN tx.category tc "
			+ "WHERE tc.type = c.type AND tx.user.id = :userId " + "GROUP BY tc.type),c.id,t.transactionDate ) "
			+ "FROM Transaction t "
			+ "JOIN t.category c " + "JOIN c.icon ci "
			+ "WHERE t.user.id = :userId and c.id = :categoryId and t.transactionDate between :fromDate and :toDate "
			+ "ORDER BY t.id DESC")
	List<TransactionData> getTransactionWithBudget(int userId, long categoryId, LocalDate fromDate, LocalDate toDate);

	List<Transaction> findByCategory_IdAndTransactionDateBetween(Long categoryId, LocalDate start, LocalDate end);

	@Query("SELECT new com.mytech.api.models.transaction.TransactionView(c.name, t.amount, ci.path, t.transactionDate) "
			+ "FROM Transaction t " + "JOIN t.category c " + "JOIN c.icon ci "
			+ "WHERE t.user.id = :userId and t.wallet.id = :walletId " + "ORDER BY t.id desc")
	Page<TransactionView> getTop5NewTransactionforWallet(int userId, int walletId, Pageable pageable);
	
	
	@Query("SELECT new com.mytech.api.models.transaction.TransactionData(" + "t.transactionId,c.name, ci.path, t.amount, c.type, "
			+ "(SELECT SUM(tx.amount) FROM Transaction tx " + "JOIN tx.category tc "
			+ "WHERE tc.type = c.type AND tx.user.id = :userId " + "GROUP BY tc.type),c.id,t.transactionDate ) " + "FROM Transaction t "
			+ "JOIN t.category c " + "JOIN c.icon ci "
			+ "WHERE t.user.id = :userId and t.savingGoal.id = :goalId " + "ORDER BY t.id DESC")
	List<TransactionData> getTransactionWithSaving(int userId,long goalId);

	@Query("SELECT new com.mytech.api.models.transaction.TransactionSavingGoalsView(t.category.name, t.amount, t.category.icon.path, t.transactionDate, t.savingGoal.id, t.user.id) FROM Transaction t WHERE t.savingGoal.id = :savingGoalId AND t.user.id = :userId")
	List<TransactionSavingGoalsView> getBySavingGoal_IdAndUser_Id(Long savingGoalId, Long userId);

}
