package com.mytech.api.controllers.wallet;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mytech.api.models.wallet.WalletType;
import com.mytech.api.services.wallet.WalletTypeService;

@RestController
@RequestMapping("/api/wallet_types")
public class WalletTypeController {

	private final WalletTypeService walletTypeService;

	public WalletTypeController(WalletTypeService walletTypeService) {
		this.walletTypeService = walletTypeService;
	}

	@GetMapping
	public ResponseEntity<List<WalletType>> getAllWalletTypes() {
		List<WalletType> walletTypes = walletTypeService.findAllWalletTypes();
		if (walletTypes.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(walletTypes, HttpStatus.OK);
	}

	@GetMapping("/{typeId}")
	public ResponseEntity<?> getWalletTypeById(@PathVariable int typeId) {
		WalletType walletType = walletTypeService.findWalletTypeById(typeId);
		if (walletType != null) {
			return new ResponseEntity<>(walletType, HttpStatus.OK);
		}
		return new ResponseEntity<>("WalletType not found with id: " + typeId, HttpStatus.NOT_FOUND);
	}
}