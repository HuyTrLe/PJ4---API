package com.mytech.api.repositories.wallet;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mytech.api.models.wallet.Wallet;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Integer> {

	List<Wallet> findByUserId(int userId);

	Page<Wallet> findByUserId(int userId, Pageable pageable);

	@Query("SELECT w FROM Wallet w WHERE w.user.id = :userId")
	List<Wallet> getWalletsByUserId(@Param("userId") int userId);

	boolean existsByWalletNameAndWalletIdNot(String name, int id);

	boolean existsByWalletNameAndUserId(String walletName, Long userId);

	boolean existsByCurrencyAndUserId(String currency, Long userId);

	boolean existsByCurrency(String currency);
}
