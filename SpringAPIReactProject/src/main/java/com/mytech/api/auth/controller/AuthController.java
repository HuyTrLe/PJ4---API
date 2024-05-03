package com.mytech.api.auth.controller;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.mytech.api.auth.UpdateUser.EmailandUsername.DTO.UserProfileDTO;
import com.mytech.api.auth.UpdateUser.EmailandUsername.UpdateEmailService;
import com.mytech.api.auth.UpdateUser.Password.DTO.PasswordChangeRequestDTO;
import com.mytech.api.auth.UpdateUser.Password.DTO.PasswordDTO;
import com.mytech.api.auth.UpdateUser.Password.UpdatePasswordService;
import com.mytech.api.auth.jwt.JwtUtils;
import com.mytech.api.auth.password.ForgotPasswordRequest;
import com.mytech.api.auth.password.ForgotPasswordService;
import com.mytech.api.auth.password.PasswordResetToken;
import com.mytech.api.auth.password.PasswordResetTokenService;
import com.mytech.api.auth.password.ResetPasswordRequest;
import com.mytech.api.auth.payload.request.EmailValidator;
import com.mytech.api.auth.payload.request.LoginRequest;
import com.mytech.api.auth.payload.request.SignupRequest;
import com.mytech.api.auth.payload.response.JwtResponse;
import com.mytech.api.auth.repositories.UserRepository;
import com.mytech.api.auth.services.MyUserDetails;
import com.mytech.api.auth.services.SignupService;
import com.mytech.api.auth.services.UserDetailServiceImpl;
import com.mytech.api.auth.services.UserService;
import com.mytech.api.models.user.User;
import com.mytech.api.models.user.UserDTO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
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
	UserService userService;

	@Autowired
	SignupService signupService;

	@Autowired
	ForgotPasswordService forgotPassService;

	@Autowired
	PasswordResetTokenService passwordResetTokenService;

	@Autowired
	ModelMapper modelMapper;

	@Autowired
	EmailValidator emailValidator;

	@Autowired
	UserDetailServiceImpl userServiceImpl;

	@Autowired
	UpdatePasswordService updatePasswordService;

	@Autowired
	UpdateEmailService updateEmailService;

	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, BindingResult result) {
		try {
			if (result.hasErrors()) {
				String errors = result.getFieldErrors().stream().map(error -> error.getDefaultMessage())
						.collect(Collectors.joining("\n"));
				System.out.println(errors);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errors);
			}
			User user = userServiceImpl.findByEmail(loginRequest.getEmail());
			if (user == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email not found");
			}
			if (!user.isEnabled()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body("You need to verify your email before login.");
			}
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

			MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
			SecurityContextHolder.getContext().setAuthentication(authentication);
			String jwt = jwtUtils.generateJwtToken(authentication);
			System.out.println("Token: " + jwt);
			return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(),
					userDetails.getEmail(), userDetails.isEnabled()));
		} catch (BadCredentialsException ex) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
		}
	}

	@PostMapping("/signup")
	public ResponseEntity<?> signup(@RequestBody @Valid SignupRequest request, BindingResult result) {
		if (result.hasErrors()) {
			String errors = result.getFieldErrors().stream().map(error -> error.getDefaultMessage())
					.collect(Collectors.joining("\n"));
			System.out.println(errors);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
		}
		ResponseEntity<?> response = signupService.signUp(request);
		return response;
	}

	@GetMapping("/signup/confirm")
	public ResponseEntity<?> confirm(@RequestParam("token") String token) {
		return signupService.confirmToken(token);
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<?> fogotPassword(@RequestBody @Valid ForgotPasswordRequest forgotPasswordRequest,
			BindingResult result) {
		if (result.hasErrors()) {
			String errors = result.getFieldErrors().stream().map(error -> error.getDefaultMessage())
					.collect(Collectors.joining("\n"));
			System.out.println(errors);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errors);
		}
		ResponseEntity<?> response = forgotPassService.forgotPassword(forgotPasswordRequest);
		return response;

	}

	@GetMapping("/validate-token/{token}")
	public ResponseEntity<?> validateToken(@PathVariable String token) {
		try {
			PasswordResetToken passwordResetToken = passwordResetTokenService.getToken(token)
					.orElseThrow(() -> new IllegalStateException("Token not found"));
			if (passwordResetToken.getExpiry().isBefore(LocalDateTime.now())) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token expired. Please try again!");
			}

			return ResponseEntity.ok("Token is valid");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token not found. Please try again!");
		}
	}

	@DeleteMapping("/password-reset-tokens/{token}")
	public ResponseEntity<String> deletePasswordResetToken(@PathVariable String token) {
		Optional<PasswordResetToken> optionalToken = passwordResetTokenService.getToken(token);
		if (optionalToken.isPresent()) {
			passwordResetTokenService.deleteTokenByTokenValue(token);
			return ResponseEntity.ok("Password reset token deleted successfully");
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Token not found");
		}
	}

	@PutMapping("/reset-password")
	public ResponseEntity<?> resetPass(@RequestBody @Valid ResetPasswordRequest passwordRequest, BindingResult result) {
		if (result.hasErrors()) {
			String errors = result.getFieldErrors().stream().map(error -> error.getDefaultMessage())
					.collect(Collectors.joining("\n"));
			System.out.println(errors);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errors);
		}
		String password = passwordRequest.getPassword();
		String token = passwordRequest.getToken();
		String confirmPassword = passwordRequest.getConfirmPassword();
		String oldPassword = userServiceImpl.getUserPasswordByResetToken(token);
		if (password.equals(oldPassword)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body("New password must be different from the old password.");
		}
		if (!password.equals(confirmPassword)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password not match");
		}
		ResponseEntity<?> response = forgotPassService.resetPassword(token, password);
		return response;
	}

	@PostMapping("/logout")
	public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null) {
			new SecurityContextLogoutHandler().logout(request, response, authentication);
		}
		return ResponseEntity.ok("Logout successful");
	}

	@DeleteMapping("/delete/{userId}")
	public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
		return userService.deleteUser(userId);
	}

	@GetMapping("/users/{userId}")
	public ResponseEntity<UserDTO> getUserId(@PathVariable Long userId) {
		Optional<User> user = userRepository.findById(userId);
		return user.map(u -> ResponseEntity.ok(modelMapper.map(u, UserDTO.class)))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping("/updateProfile/updatePassword")
	@PreAuthorize("#passwordDTO.userId == authentication.principal.id")
	public ResponseEntity<?> updateUserPassword(@Valid @RequestBody PasswordDTO passwordDTO,
			BindingResult result) {
		if (result.hasErrors()) {
			String errors = result.getFieldErrors().stream().map(error -> error.getDefaultMessage())
					.collect(Collectors.joining("\n"));
			System.out.println(errors);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errors);
		}
		try {
			ResponseEntity<?> response = updatePasswordService.updateUserPassword(passwordDTO);
			return response;
		} catch (Exception e) {
			return ResponseEntity.status(500).body("Internal Server Error");
		}
	}

	@PostMapping("/updateProfile/changePasswordWithOTP")
	@PreAuthorize("#requestDTO.passwordDTO.userId == authentication.principal.id")
	public ResponseEntity<?> changePasswordWithOTP(@RequestBody PasswordChangeRequestDTO requestDTO) {
		try {
			ResponseEntity<?> response = updatePasswordService.changePasswordWithOTP(requestDTO);
			return response;
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
		}
	}

	@PutMapping("/updateEmailUsernameProfile/{userId}")
	public ResponseEntity<?> updateUser(@PathVariable Long userId, @Valid @RequestBody UserProfileDTO userDTO,
			BindingResult result) {
		if (result.hasErrors()) {
			String errors = result.getFieldErrors().stream().map(error -> error.getDefaultMessage())
					.collect(Collectors.joining("\n"));
			System.out.println(errors);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errors);
		}
		return updateEmailService.updateUser(userId, userDTO);
	}

	@GetMapping("/updateEmailUsernameProfile/confirmToken")
	public ResponseEntity<String> confirmToken(@RequestParam String token) {
		return updateEmailService.confirmToken(token);
	}

}
