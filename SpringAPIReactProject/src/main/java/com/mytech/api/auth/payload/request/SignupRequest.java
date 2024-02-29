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
	private String password;

}
