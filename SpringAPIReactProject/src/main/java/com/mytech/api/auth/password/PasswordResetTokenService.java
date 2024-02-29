package com.mytech.api.auth.password;

import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PasswordResetTokenService {

	private final PasswordResetTokenRepository passwordResetTokenRepository;
	
    
	public PasswordResetToken save(PasswordResetToken passwordResetToken) {
		return passwordResetTokenRepository.save(passwordResetToken);
	}
	
	public Optional<PasswordResetToken> getToken (String token){
		return passwordResetTokenRepository.findByToken(token);
	}
   
}
