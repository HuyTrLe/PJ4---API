package com.mytech.api.models.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.mytech.api.models.category.Category;
import com.mytech.api.models.recurrence.Recurrence;
import com.mytech.api.models.user.User;
import com.mytech.api.models.wallet.Wallet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions")
public class Transaction {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int transactionId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column
    private String notes;

    @ManyToOne
    @JoinColumn(name = "recurrence_id")
    private Recurrence recurrence;

    @Column(nullable = false, length = 3)
    private String currency;
}
