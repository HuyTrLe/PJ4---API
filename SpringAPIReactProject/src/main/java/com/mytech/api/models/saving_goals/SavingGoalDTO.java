
package com.mytech.api.models.saving_goals;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class SavingGoalDTO {
    private Long id;
    private String name;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long userId;
    private Long walletId;
    
    
	public SavingGoalDTO(Long id, String name, BigDecimal targetAmount, BigDecimal currentAmount, LocalDate startDate,
			LocalDate endDate, Long userId, Long walletId) {
		super();
		this.id = id;
		this.name = name;
		this.targetAmount = targetAmount;
		this.currentAmount = currentAmount;
		this.startDate = startDate;
		this.endDate = endDate;
		this.userId = userId;
		this.walletId = walletId;
	}    
}
