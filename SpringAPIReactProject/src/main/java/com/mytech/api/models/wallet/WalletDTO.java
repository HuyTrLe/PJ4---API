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
    
    public BigDecimal updateBalance(TransactionDTO transaction) {
        String transactionType = transaction.getCategory().getType().toString(); 
        BigDecimal updatedBalance = calculateUpdatedBalance(transaction.getAmount(), this.balance, transactionType);
        if (updatedBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Balance is not enough");
        }
        this.balance = updatedBalance;
        return this.balance;
    }

    private BigDecimal calculateUpdatedBalance(BigDecimal amount, BigDecimal currentBalance, String transactionType) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (currentBalance == null) {
            throw new IllegalArgumentException("Current balance cannot be null");
        }

        if ("INCOME".equalsIgnoreCase(transactionType)) {
            return currentBalance.add(amount);
        } else if ("EXPENSE".equalsIgnoreCase(transactionType)) {
            return currentBalance.subtract(amount);
        }
        return currentBalance;
    }
}
