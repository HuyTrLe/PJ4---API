package com.mytech.api.auth.payload.request.token;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import com.mytech.api.models.user.User;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ConfirmationTokenService {
    private final ConfirmationTokenRepository confirmationTokenRepository;

    public void saveConfirmationToken(ConfirmationToken token) {
        confirmationTokenRepository.save(token);
    }

    public Optional<ConfirmationToken> getToken(String token) {
        return confirmationTokenRepository.findByToken(token);
    }

    public int setConfirmedAt(String token) {
        return confirmationTokenRepository.updateConfirmedAt(
                token, LocalDateTime.now());
    }
    
    public Optional<ConfirmationToken> getTokenByUser(User user) {
		return confirmationTokenRepository.findByUser(user);
	}
}
