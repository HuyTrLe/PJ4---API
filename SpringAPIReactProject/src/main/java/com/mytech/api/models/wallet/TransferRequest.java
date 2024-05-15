package com.mytech.api.models.wallet;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {
    private Long id;
    private Long userId;
    private int sourceWalletId;
    private int destinationWalletId;
    private BigDecimal amount;
    private BigDecimal exchangeRate;
    private LocalDate transferDate;
    private int transactionId;
}