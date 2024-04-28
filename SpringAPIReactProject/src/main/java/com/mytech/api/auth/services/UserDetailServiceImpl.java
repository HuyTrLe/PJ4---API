package com.mytech.api.auth.services;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mytech.api.auth.password.PasswordResetToken;
import com.mytech.api.auth.password.PasswordResetTokenService;
import com.mytech.api.auth.payload.request.token.ConfirmationToken;
import com.mytech.api.auth.payload.request.token.ConfirmationTokenService;
import com.mytech.api.auth.repositories.UserRepository;
import com.mytech.api.models.user.User;
import com.mytech.api.services.category.CategoryService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {
	@Autowired
	UserRepository userRepository;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	ConfirmationTokenService confirmationTokenService;

	@Autowired
	PasswordResetTokenService passwordResetTokenService;

	@Autowired
	CategoryService categoryService;

	@Autowired
	ModelMapper modelMapper;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		System.out.println(email);
		if (email == null) {
			throw new UsernameNotFoundException("Email cannot be null");
		}

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("User Not Found with email: " + email));
		return MyUserDetails.build(user);
	}

	public User findByEmail(String email) {
		return userRepository.findByEmail(email).orElse(null);
	}

	public User findByUsername(String username) {
		return userRepository.findByUsername(username).orElse(null);
	}

	public User save(User user) {
		return userRepository.save(user);
	}

	public String signUpUser(User user) {
		Optional<User> existingUserOptional = userRepository.findByEmail(user.getEmail());

		if (existingUserOptional.isPresent()) {
			User existingUser = existingUserOptional.get();
			if (user.getUsername() != null && !user.getUsername().isEmpty()) {
				existingUser.setUsername(user.getUsername());
			}
			Optional<ConfirmationToken> existingTokenOptional = confirmationTokenService.getTokenByUser(existingUser);

			if (existingTokenOptional.isPresent()) {
				ConfirmationToken tokenToUpdate = existingTokenOptional.get();
				tokenToUpdate.setCreatedAt(LocalDateTime.now());
				tokenToUpdate.setExpiresAt(LocalDateTime.now().plusMinutes(15));
				confirmationTokenService.saveConfirmationToken(tokenToUpdate);
				return tokenToUpdate.getToken();
			} else {
				String token = UUID.randomUUID().toString();
				ConfirmationToken confirmationToken = new ConfirmationToken(token, LocalDateTime.now(),
						LocalDateTime.now().plusMinutes(15), existingUser);
				confirmationTokenService.saveConfirmationToken(confirmationToken);
				categoryService.seedCategoriesForNewUsers(existingUser);
				return token;
			}
		} else {
			String encodedPassword = encoder.encode(user.getPassword());
			user.setPassword(encodedPassword);
			User savedUser = userRepository.save(user);
			String token = UUID.randomUUID().toString();
			ConfirmationToken confirmationToken = new ConfirmationToken(token, LocalDateTime.now(),
					LocalDateTime.now().plusMinutes(15), savedUser);
			confirmationTokenService.saveConfirmationToken(confirmationToken);
			categoryService.seedCategoriesForNewUsers(savedUser);
			return token;
		}
	}

	public String forgotPassword(User user) {
		Optional<PasswordResetToken> existingToken = passwordResetTokenService.getTokenByUser(user);

		if (existingToken.isPresent()) {
			PasswordResetToken tokenToUpdate = existingToken.get();
			tokenToUpdate.setToken(UUID.randomUUID().toString());
			tokenToUpdate.setCreatedAt(LocalDateTime.now());
			tokenToUpdate.setExpiry(LocalDateTime.now().plusMinutes(15));
			passwordResetTokenService.save(tokenToUpdate);
			return tokenToUpdate.getToken();

		} else {
			String token = UUID.randomUUID().toString();
			PasswordResetToken passwordResetToken = new PasswordResetToken(token, LocalDateTime.now(),
					LocalDateTime.now().plusMinutes(15), user);
			passwordResetTokenService.save(passwordResetToken);
			return token;
		}
	}

	public String getUserPasswordByResetToken(String resetToken) {
		Optional<PasswordResetToken> passwordResetTokens = passwordResetTokenService.getToken(resetToken);
		if (passwordResetTokens.isPresent()) {
			PasswordResetToken passwordResetToken = passwordResetTokens.get();
			User user = passwordResetToken.getUser();
			if (user != null) {
				return user.getPassword();
			}
		}
		return null;
	}

	public void enabledUser(String email) {
		userRepository.enabledUser(email);
	}
}
