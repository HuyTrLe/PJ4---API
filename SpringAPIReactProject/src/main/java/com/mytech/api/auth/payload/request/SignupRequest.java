package com.mytech.api.auth.payload.request;


import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {
	@NotBlank
	@Column(unique = true)
	private String username;

	@NotBlank
	@Email
	@Column(unique = true)
	private String email;

	@NotBlank
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$", message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, at least 8 characters long.")
	private String password;
	
	@NotBlank
	private String confirmPassword;

}
