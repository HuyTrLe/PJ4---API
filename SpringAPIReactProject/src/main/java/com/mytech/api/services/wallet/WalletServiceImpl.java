package com.mytech.api.services.wallet;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mytech.api.auth.repositories.UserRepository;
import com.mytech.api.models.wallet.Wallet;
import com.mytech.api.repositories.wallet.WalletRepository;

@Service
public class WalletServiceImpl implements WalletService {

	private final WalletRepository walletRepository;

	public WalletServiceImpl(WalletRepository walletRepository, UserRepository userRepository) {
		this.walletRepository = walletRepository;
	}

	@Override
	public Wallet saveWallet(Wallet wallet) {
		return walletRepository.save(wallet);
	}

	@Override
	public Wallet getWalletById(int walletId) {
		return walletRepository.findById(walletId).orElse(null);
	}

	@Override
	public List<Wallet> getWalletsByUserId(int userId) {
		return walletRepository.findByUserId(userId);
	}

	@Override
	public void deleteWallet(int walletId) {
		walletRepository.deleteById(walletId);
	}
}
