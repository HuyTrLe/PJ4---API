
package com.mytech.api.models.saving_goals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.mytech.api.models.transaction.Transaction;
import com.mytech.api.models.transaction.TransactionRecurring;
import com.mytech.api.models.user.User;
import com.mytech.api.models.wallet.Wallet;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "saving_goals")
@Getter
@Setter
@NoArgsConstructor
public class SavingGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal targetAmount;

    @Column(nullable = false)
    private BigDecimal currentAmount;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private EndDateType endDateType;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @OneToMany(mappedBy = "savingGoal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transaction = new ArrayList<>();

    @OneToMany(mappedBy = "savingGoal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionRecurring> transactionsRecurring = new ArrayList<>();

    public SavingGoal(Long id, String name, BigDecimal targetAmount, BigDecimal currentAmount, LocalDate startDate,
            LocalDate endDate, User user, Wallet wallet) {
        super();
        this.id = id;
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.user = user;
        this.wallet = wallet;
    }

}