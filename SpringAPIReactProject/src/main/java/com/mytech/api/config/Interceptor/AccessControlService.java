package com.mytech.api.config.Interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mytech.api.auth.repositories.UserRepository;

@Service
public class AccessControlService {

    @Autowired
    private UserRepository userRepository;

    public boolean hasAccess(String loggedInUserEmail, int userId) {
        // Lấy user ID từ email của người dùng đăng nhập
        Long loggedInUserId = userRepository.findByEmail(loggedInUserEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + loggedInUserEmail))
                .getId();

        // So sánh user ID từ email với user ID trong request
        return loggedInUserId.equals((long) userId);
    }
}
