package com.mytech.api.services.wallet;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mytech.api.models.wallet.WalletType;
import com.mytech.api.repositories.wallet.WalletTypeRepository;

@Service
public class WalletTypeServiceImpl implements WalletTypeService {
	private final WalletTypeRepository typeRepository;

	public WalletTypeServiceImpl(WalletTypeRepository typeRepository) {
		this.typeRepository = typeRepository;
		seedWalletTypes();
	}

	private void seedWalletTypes() {
		if (typeRepository.count() == 0) {
			typeRepository.save(new WalletType(1, "Cash"));
			typeRepository.save(new WalletType(2, "ATM"));
			typeRepository.save(new WalletType(3, "Goals"));
		}
	}

	@Override
	public List<WalletType> findAllWalletTypes() {
		return typeRepository.findAll();
	}

	@Override
	public WalletType findWalletTypeById(int typeId) {
		return typeRepository.findById(typeId).orElse(null);
	}

}
