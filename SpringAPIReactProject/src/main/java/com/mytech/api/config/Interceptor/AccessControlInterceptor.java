package com.mytech.api.config.Interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.mytech.api.auth.repositories.UserRepository;
import com.mytech.api.auth.services.MyUserDetails;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AccessControlInterceptor implements HandlerInterceptor {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            String loggedInUserEmail = ((MyUserDetails) userDetails).getEmail();

            Long loggedInUserId = userRepository.findByEmail(loggedInUserEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + loggedInUserEmail))
                    .getId();

            int userId = extractUserIdFromRequest(request);

            // Check if the user ID from the path variable matches the logged-in user's ID
            if (loggedInUserId != userId) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("You are not authorized to access of other users.");
                return false;
            }
            return true;
        } else {
            throw new UsernameNotFoundException("User details not found in the authentication principal.");
        }
    }

    private int extractUserIdFromRequest(HttpServletRequest request) {
        // Lấy URI từ request
        String uri = request.getRequestURI();

        Pattern pattern = Pattern.compile("/users?/(\\d+)$");
        Matcher matcher = pattern.matcher(uri);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        } else {
            return -1;
        }
    }

}
