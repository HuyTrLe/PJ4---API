package com.mytech.api.auth.password;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ResetPasswordRequest {
	private String token;
    private String password;

}
