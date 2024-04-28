package com.mytech.api.models.wallet;

import java.math.BigDecimal;

import com.mytech.api.models.transaction.TransactionDTO;

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

	public BigDecimal addTransactionBalance(TransactionDTO transaction) {
		BigDecimal amountTransaction = transaction.getAmount();
		String transactionType = transaction.getCategory().getType().toString();

		if ("INCOME".equalsIgnoreCase(transactionType)) {
			this.balance = this.balance.add(amountTransaction);
		} else {
			this.balance = this.balance.subtract(amountTransaction);
		}
		if (this.balance.compareTo(BigDecimal.ZERO) < 0) {
			throw new RuntimeException("Balance is not enough");
		}

		return this.balance;
	}

}
