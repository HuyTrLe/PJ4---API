package com.mytech.api.repositories.wallet;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mytech.api.models.wallet.Wallet;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Integer>{

	List<Wallet> findByUserId(int userId);
}
