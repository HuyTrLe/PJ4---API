package com.mytech.api.auth.password;

import java.time.LocalDateTime;

import com.mytech.api.models.user.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class PasswordResetToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	private String token;
	private LocalDateTime expiry;
	private LocalDateTime createdAt;
	private LocalDateTime confirmedAt;

	@OneToOne(targetEntity = User.class, fetch = FetchType.LAZY)
	private User user;

	public PasswordResetToken(String token, LocalDateTime createdAt, LocalDateTime expiry, User user) {
		this.token = token;
		this.createdAt = createdAt;
		this.expiry = expiry;
		this.user = user;
	}
}
