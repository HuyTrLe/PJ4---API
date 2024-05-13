
package com.mytech.api.models.saving_goals;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SavingGoalDTO {
	private Long id;

	@NotBlank(message = "Name cannot be blank")
	private String name;

	@Min(value = 0, message = "Target amount must be greater than or equal to 0")
	@NotNull(message = "Target Amount cannot be null")
	private BigDecimal targetAmount;

	@NotNull(message = "Current Amount cannot be null")
	private BigDecimal currentAmount;
	@FutureOrPresent(message = "Start date must be in future or present")
	private LocalDate startDate;
	private LocalDate endDate;
	private EndDateType endDateType;
	private Long userId;
	private Integer walletId;

	// public SavingGoalDTO(Long id, String name, BigDecimal targetAmount,
	// BigDecimal currentAmount, LocalDate startDate,
	// LocalDate endDate, EndDateType endDateType, Long userId, Long walletId) {
	// super();
	// this.id = id;
	// this.name = name;
	// this.targetAmount = targetAmount;
	// this.currentAmount = currentAmount;
	// this.startDate = startDate;
	// this.endDate = endDate;
	// this.endDateType = endDateType;
	// this.userId = userId;
	// this.walletId = walletId;
	// }
}
