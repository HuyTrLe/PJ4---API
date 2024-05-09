package com.mytech.api.models.transaction;

import java.math.BigDecimal;

import com.mytech.api.models.category.Category;
import com.mytech.api.models.recurrence.Recurrence;
import com.mytech.api.models.saving_goals.SavingGoal;
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
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions_recurring")
public class TransactionRecurring {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int transactionRecurringId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column
    private String notes;

    @ManyToOne
    @JoinColumn(name = "recurrence_id")
    private Recurrence recurrence;

    @ManyToOne
    @JoinColumn(name = "saving_goal_id")
    private SavingGoal savingGoal;
}
