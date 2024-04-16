
package com.mytech.api.models.debt;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class DebtDTO {
    private Long id;
	private String name;
    private Long userId;
    private Long categoryId;
    private Long recurrenceId;
    private String creditor;
    private BigDecimal amount;
    private LocalDate startDate;
    private LocalDate endDate;
    private String notes;
    
	public DebtDTO(Long id, String name, Long userId, Long categoryId, Long recurrenceId, String creditor,
			BigDecimal amount, LocalDate startDate, LocalDate endDate, String notes) {
		super();
		this.id = id;
		this.name = name;
		this.userId = userId;
		this.categoryId = categoryId;
		this.recurrenceId = recurrenceId;
		this.creditor = creditor;
		this.amount = amount;
		this.startDate = startDate;
		this.endDate = endDate;
		this.notes = notes;
	}

    
}