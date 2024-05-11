package com.mytech.api.models.wallet;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequest {
    private Long userId;
    private int sourceWalletId;
    private int destinationWalletId;
    private BigDecimal amount;
    private BigDecimal exchangeRate;

}
