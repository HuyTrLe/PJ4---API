package com.mytech.api.models.user;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mytech.api.auth.password.PasswordResetToken;
import com.mytech.api.auth.payload.request.token.ConfirmationToken;
import com.mytech.api.models.category.Category;
import com.mytech.api.models.recurrence.Recurrence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"categories", "recurrences", "confirmationTokens", "passwordResetToken"})
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Size(max = 20)
	@Column(unique = true)
	private String username;

	@NotBlank
	@Size(max = 50)
	@Email
	@Column(unique = true)
	private String email;

	@NotBlank
	@Size(max = 120)
	private String password;

	private boolean isEnabled = false;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Category> categories = new ArrayList<>();

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Recurrence> recurrences = new ArrayList<>();

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ConfirmationToken> confirmationTokens = new ArrayList<>();

	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private PasswordResetToken passwordResetToken;

	public User(String username, String email, String password, boolean isEnabled) {
		this.username = username;
		this.email = email;
		this.password = password;
		this.isEnabled = isEnabled;
	}

}
