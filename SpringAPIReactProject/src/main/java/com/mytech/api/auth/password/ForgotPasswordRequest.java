package com.mytech.api.auth.password;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ForgotPasswordRequest {
	@NotBlank(message = "Email cannot be blank")
	@Email
	private String email;
}
