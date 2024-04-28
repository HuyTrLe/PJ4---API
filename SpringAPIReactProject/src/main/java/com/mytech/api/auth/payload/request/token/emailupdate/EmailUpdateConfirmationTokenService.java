package com.mytech.api.auth.payload.request.token.emailupdate;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class EmailUpdateConfirmationTokenService {
	private final EmailUpdateConfirmationTokenRepository emailConfirmationTokenRepository;

	public EmailUpdateConfirmationToken save(EmailUpdateConfirmationToken emailUpdateConfirmationToken) {
		return emailConfirmationTokenRepository.save(emailUpdateConfirmationToken);
	}

	public Optional<EmailUpdateConfirmationToken> getToken(String token) {
		return emailConfirmationTokenRepository.findByToken(token);
	}

	public long setConfirmedAt(String token) {
		return emailConfirmationTokenRepository.updateConfirmedAt(
				token, LocalDateTime.now());
	}
}
