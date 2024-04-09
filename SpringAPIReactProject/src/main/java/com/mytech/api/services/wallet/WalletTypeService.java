package com.mytech.api.services.wallet;

import java.util.List;

import com.mytech.api.models.wallet.WalletType;

public interface WalletTypeService {
	List<WalletType> findAllWalletTypes();

	WalletType findWalletTypeById(int typeId);
}
