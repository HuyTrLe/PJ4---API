package com.mytech.api.services.wallet;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mytech.api.models.wallet.WalletType;
import com.mytech.api.repositories.wallet.WalletTypeRepository;

@Service
public class WalletTypeServiceImpl implements WalletTypeService{
	private final WalletTypeRepository typeRepository;
	
	public WalletTypeServiceImpl(WalletTypeRepository typeRepository) {
		this.typeRepository = typeRepository;
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
