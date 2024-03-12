package com.mytech.api.auth.payload.request;


import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SignupRequest {
	@NotBlank(message = "Username cannot be blank")
	@Column(unique = true)
	private String username;

	@NotBlank(message = "Email cannot be blank")
	@Email
	@Column(unique = true)
	private String email;

	@NotBlank(message = "Password cannot be blank")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$", message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, at least 8 characters long.")
	private String password;
	
	@NotBlank(message = "Confirm password cannot be blank")
	private String confirmPassword;

}
