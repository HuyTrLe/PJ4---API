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
    private BigDecimal amount;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long userId;
    
    
	public DebtDTO(Long id, String name, BigDecimal amount, LocalDate startDate, LocalDate endDate, Long userId) {
		super();
		this.id = id;
		this.name = name;
		this.amount = amount;
		this.startDate = startDate;
		this.endDate = endDate;
		this.userId = userId;
	}

    
}
