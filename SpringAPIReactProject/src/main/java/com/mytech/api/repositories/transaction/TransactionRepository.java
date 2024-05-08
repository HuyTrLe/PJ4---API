package com.mytech.api.repositories.transaction;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mytech.api.models.budget.ParamBudget;
import com.mytech.api.models.transaction.Transaction;
import com.mytech.api.models.transaction.TransactionData;
import com.mytech.api.models.transaction.TransactionReport;
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

	@Query("SELECT new com.mytech.api.models.transaction.TransactionView(c.name, t.amount, ci.path) "
			+ "FROM Transaction t " + "JOIN t.category c " + "JOIN c.icon ci "
			+ "WHERE t.user.id = :userId order by t.id desc")
	Page<TransactionView> getTop5NewTransaction(int userId, Pageable pageable);

	@Query("SELECT NEW com.mytech.api.models.transaction.TransactionView(t.category.name, t.amount, c.icon.path) "
			+ "FROM Transaction t " + "JOIN t.category c "
			+ "WHERE t.user.id = :userId and c.type = com.mytech.api.models.category.CateTypeENum.EXPENSE "
			+ "ORDER BY t.amount DESC")
	Page<TransactionView> getTop5TransactionHightestMoney(int userId, Pageable pageable);

	@Query("SELECT new com.mytech.api.models.transaction.TransactionData(" + "c.name, ci.path, t.amount, c.type, "
			+ "(SELECT SUM(tx.amount) FROM Transaction tx " + "JOIN tx.category tc "
			+ "WHERE tc.type = c.type AND tx.user.id = :userId " + "GROUP BY tc.type) ) " + "FROM Transaction t "
			+ "JOIN t.category c " + "JOIN c.icon ci "
			+ "WHERE t.user.id = :userId and t.transactionDate between :fromDate and :toDate " + "ORDER BY t.id DESC")
	List<TransactionData> getTransactionWithTime(int userId, LocalDate fromDate, LocalDate toDate);

//	@Query("SELECT new com.mytech.api.models.transaction.TransactionReport(" +
//            "t.transactionDate, " +
//            "(SELECT SUM(tx.amount) FROM Transaction tx " +
//            "JOIN tx.category tc " +
//            "WHERE tc.type = c.type AND tx.user.id = :userId and tc.type = com.mytech.api.models.category.CateTypeENum.EXPENSE " +
//            "GROUP BY tx.transactionDate) ) " +
//            "FROM Transaction t " +
//            "JOIN t.category c " +
//            "JOIN c.icon ci " +
//            "WHERE t.user.id = :userId and t.transactionDate between :fromDate and :toDate and c.type = com.mytech.api.models.category.CateTypeENum.EXPENSE " +
//            "GROUP BY t.transactionDate " +
//            "ORDER BY t.transactionDate ASC")
	@Query("SELECT new com.mytech.api.models.transaction.TransactionReport(" + "t.transactionDate, " + "SUM(t.amount)) "
			+ "FROM Transaction t " + "JOIN t.category c " + "JOIN c.icon ci "
			+ "WHERE t.user.id = :userId and t.transactionDate between :fromDate and :toDate and c.type = com.mytech.api.models.category.CateTypeENum.EXPENSE "
			+ "GROUP BY t.transactionDate " + "ORDER BY t.transactionDate ASC")
	List<TransactionReport> getTransactionReport(int userId, LocalDate fromDate, LocalDate toDate);

	List<Transaction> findByCategory_IdAndTransactionDateBetween(Long categoryId, LocalDate start, LocalDate end);

	@Query("SELECT new com.mytech.api.models.transaction.TransactionView(c.name, t.amount, ci.path, t.transactionDate) "
            + "FROM Transaction t " + "JOIN t.category c " + "JOIN c.icon ci "
            + "WHERE t.user.id = :userId and t.wallet.id = :walletId " + "ORDER BY t.id desc")
	Page<TransactionView> getTop5NewTransactionforWallet(int userId, int walletId, Pageable pageable);
}
