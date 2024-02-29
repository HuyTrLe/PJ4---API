package com.mytech.api.auth.controller;


import jakarta.validation.Valid;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mytech.api.auth.password.ForgotPasswordRequest;
import com.mytech.api.auth.password.ForgotPasswordService;
import com.mytech.api.auth.password.PasswordResetToken;
import com.mytech.api.auth.password.PasswordResetTokenService;
import com.mytech.api.auth.password.ResetPasswordRequest;
import com.mytech.api.auth.payload.request.LoginRequest;
import com.mytech.api.auth.payload.request.SignupRequest;
import com.mytech.api.auth.payload.response.JwtResponse;
import com.mytech.api.auth.repositories.UserRepository;
import com.mytech.api.auth.security.jwt.JwtUtils;
import com.mytech.api.auth.security.services.MyUserDetails;
import com.mytech.api.auth.security.services.SignupService;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/auth")
public class AuthController {
  @Autowired
  AuthenticationManager authenticationManager;

  @Autowired
  UserRepository userRepository;

  @Autowired
  PasswordEncoder encoder;

  @Autowired
  JwtUtils jwtUtils;
  
  @Autowired
  SignupService signupService;
  
  @Autowired
  ForgotPasswordService forgotPassService;
  
  @Autowired
  PasswordResetTokenService passwordResetTokenService;

  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
	  try {
		  System.out.println("Received login request: " + loginRequest.getUsername() + ", " + loginRequest.getPassword());
	        Authentication authentication = authenticationManager.authenticate(
	                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

	        SecurityContextHolder.getContext().setAuthentication(authentication);
	        String jwt = jwtUtils.generateJwtToken(authentication);

	        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
	        System.out.println("Token: " + jwt);
	        return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), userDetails.isEnabled()));
	    } catch (BadCredentialsException ex) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
	    }
  }


  @PostMapping("/signup")
  public String signup(@RequestBody SignupRequest request) {
	  return signupService.signUp(request);
  }
  
  @GetMapping(path = "/signup/confirm")
  public ResponseEntity<String> confirm(@RequestParam("token") String token) {
      ResponseEntity<String> confirmationResponse = signupService.confirmToken(token);
      return confirmationResponse;
  }
  
  @PostMapping("/forgot-password")
  public String fogotPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest) {
	  String email = forgotPasswordRequest.getEmail();
	  return forgotPassService.forgotPassword(email);
  }
  
  @GetMapping("/validate-token/{token}")
  public ResponseEntity<String> validateToken(@PathVariable String token) {
      try {
          PasswordResetToken passwordResetToken = passwordResetTokenService.getToken(token)
                  .orElseThrow(() -> new IllegalStateException("Token not found"));
          if (passwordResetToken.getExpiry().isBefore(LocalDateTime.now())) {
              return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token expired");
          }
          
          return ResponseEntity.ok("Token is valid");
      } catch (Exception e) {
          return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token");
      }
  }
  
  @PutMapping("/reset-password")
  public ResponseEntity<String> resetPass(@RequestBody ResetPasswordRequest passwordRequest) {
      String password = passwordRequest.getPassword();
      String token = passwordRequest.getToken();
      ResponseEntity<String> response = forgotPassService.resetPassword(token, password);
      return response;
  }

}
