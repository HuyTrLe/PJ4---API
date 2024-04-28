package com.mytech.api.auth.payload.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtResponse {
	private String token;
	private String type = "Bearer";
	private long id;
	private String username;
	private String email;
	private boolean isEnabled;

	public JwtResponse(String accessToken, long id, String username, String email, boolean isEnabled) {
		this.token = accessToken;
		this.id = id;
		this.username = username;
		this.email = email;
		this.isEnabled = isEnabled;
	}

	public String getAccessToken() {
		return token;
	}

	public void setAccessToken(String accessToken) {
		this.token = accessToken;
	}

}
