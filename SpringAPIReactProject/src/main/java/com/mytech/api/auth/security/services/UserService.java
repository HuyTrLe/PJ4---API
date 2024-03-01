package com.mytech.api.auth.security.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mytech.api.auth.password.PasswordResetTokenRepository;
import com.mytech.api.auth.payload.request.token.ConfirmationTokenRepository;
import com.mytech.api.auth.repositories.UserRepository;
import com.mytech.api.repositories.categories.CategoryRepository;

import jakarta.transaction.Transactional;


@Service
public class UserService {
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	ConfirmationTokenRepository confirmationTokenRepository;
	
	@Autowired
	PasswordResetTokenRepository passwordResetTokenRepository;
	
	@Autowired
	CategoryRepository categoryRepository;
	
	@Transactional
    public void deleteUser(Long userId) {
        
        confirmationTokenRepository.deleteByUserId(userId);
        passwordResetTokenRepository.deleteByUserId(userId);
        categoryRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);
    }
}
