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
import com.mytech.api.models.user.UserDTO;
import com.mytech.api.services.category.CategoryService;

import jakarta.transaction.Transactional;
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
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
		UserDTO userDTO = modelMapper.map(user, UserDTO.class);
		return MyUserDetails.build(userDTO);
	}
	
	@Transactional
	public User findByEmail(String email){
		Optional<User> userOptional = userRepository.findByEmail(email);
		return userOptional.orElse(null);
	}
	
	 public User save(User user) {
	        return userRepository.save(user);
	}

	public String signUpUser(User user) {

		String encodedPassword = encoder.encode(user.getPassword());
		user.setPassword(encodedPassword);
		userRepository.save(user);
		String token = UUID.randomUUID().toString();
		ConfirmationToken confirmationToken = new ConfirmationToken(token, LocalDateTime.now(),
				LocalDateTime.now().plusMinutes(15), user);
		confirmationTokenService.saveConfirmationToken(confirmationToken);
		categoryService.seedCategoriesForNewUsers(user);
		return token;
	}
	

	public String forgotPassword(User user) {
		String token = UUID.randomUUID().toString();
		PasswordResetToken passwordResetToken = new PasswordResetToken(token, LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15), user);
		passwordResetTokenService.save(passwordResetToken);
		return token;
	}
	
	
	public void enabledUser(String email) {
		userRepository.enabledUser(email);
	}
}
