package com.mytech.api.auth.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mytech.api.auth.repositories.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService {
	@Autowired
	UserRepository userRepository;

	@Transactional
	public void deleteUser(Long userId) {
		userRepository.deleteById(userId);
	}
}
