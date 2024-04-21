package com.mytech.api.repositories.wallet;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mytech.api.models.wallet.Wallet;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Integer> {

	List<Wallet> findByUserId(int userId);

	@Query("SELECT SUM(w.balance) FROM Wallet w WHERE w.user.id = ?1")
	BigDecimal getTotalBalanceForUser(int userId);

	@Query("SELECT w FROM Wallet w WHERE w.user.id = :userId")
	List<Wallet> getWalletsByUserId(@Param("userId") int userId);
}
