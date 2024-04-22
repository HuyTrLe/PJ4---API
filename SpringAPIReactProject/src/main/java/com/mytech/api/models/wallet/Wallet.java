package com.mytech.api.models.wallet;

import com.mytech.api.models.saving_goals.SavingGoal;
import com.mytech.api.models.transaction.Transaction;
import com.mytech.api.models.user.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "wallet")
public class Wallet {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int walletId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@Column(name = "wallet_name", nullable = false)
	private String walletName;

	@Column(name = "balance", nullable = false)
	private BigDecimal balance;

	@Column(name = "bank_name")
	private String bankName;

	@Column(name = "bank_accountnum")
	private String bankAccountNum;

	@Column(name = "wallet_type", nullable = false)
	private int walletType;

	@Column(name = "currency", nullable = false)
	private String currency;

	@OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL)
	private List<Transaction> transactions = new ArrayList<>();

}