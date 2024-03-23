package com.mytech.api.auth.password;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.mytech.api.models.user.User;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PasswordResetTokenService {

	private final PasswordResetTokenRepository passwordResetTokenRepository;

	public PasswordResetToken save(PasswordResetToken passwordResetToken) {
		return passwordResetTokenRepository.save(passwordResetToken);
	}

	public Optional<PasswordResetToken> getToken(String token) {
		return passwordResetTokenRepository.findByToken(token);
	}

	public Optional<PasswordResetToken> getTokenByUser(User user) {
		return passwordResetTokenRepository.findByUser(user);
	}

	public int setConfirmedAt(String token) {
		return passwordResetTokenRepository.updateConfirmedAt(token, LocalDateTime.now());
	}

	@Transactional
    public void deleteTokenByTokenValue(String tokenValue) {
        Optional<PasswordResetToken> token = passwordResetTokenRepository.findByToken(tokenValue);
        token.ifPresent(passwordResetTokenRepository::delete);
    }

}
