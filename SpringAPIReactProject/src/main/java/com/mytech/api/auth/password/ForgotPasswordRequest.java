package com.mytech.api.auth.password;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ForgotPasswordRequest {
	@Email
	private String email;
}
