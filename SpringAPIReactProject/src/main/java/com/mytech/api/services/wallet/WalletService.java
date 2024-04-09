package com.mytech.api.services.wallet;

import java.util.List;

import com.mytech.api.models.wallet.Wallet;

public interface WalletService {
	List<Wallet> getWalletsByUserId(int userId);

	Wallet saveWallet(Wallet wallet);

	Wallet getWalletById(int walletId);

	void deleteWallet(int walletId);
}
