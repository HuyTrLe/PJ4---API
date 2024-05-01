package com.mytech.api.models.wallet;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WalletDTO {
	private int walletId;
	private int userId;
	private String walletName;
	private BigDecimal balance;
	private String bankName;
	private String bankAccountNum;
	private int walletType;
	private String currency;

}
