package com.mytech.api.services.wallet;

import java.util.List;

import com.mytech.api.models.wallet.TransferRequest;
import com.mytech.api.models.wallet.Wallet;
import com.mytech.api.models.wallet.WalletDTO;

public interface WalletService {
	List<Wallet> getWalletsByUserId(int userId);

	WalletDTO createWallet(WalletDTO walletDTO);

	Wallet getWalletById(int walletId);

	void deleteWallet(int walletId);

	WalletDTO updateWallet(int walletId, WalletDTO walletDTO);

	void transferUSDToVND(TransferRequest transferRequest);

}
