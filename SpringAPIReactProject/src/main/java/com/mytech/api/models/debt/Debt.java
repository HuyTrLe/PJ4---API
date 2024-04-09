package com.mytech.api.models.debt;

import com.mytech.api.models.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "debts")
@Getter
@Setter
@NoArgsConstructor
public class Debt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

	public Debt(Long id, String name, BigDecimal amount, LocalDate startDate, LocalDate endDate, User user) {
		super();
		this.id = id;
		this.name = name;
		this.amount = amount;
		this.startDate = startDate;
		this.endDate = endDate;
		this.user = user;
	}

    
}
