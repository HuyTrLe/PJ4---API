package com.mytech.api.models.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDTO {
	private String token;
	private Long id;
	private String username;
	private String email;
	private String password;
	private boolean isEnabled;
	private boolean confirmNewEmail = false;
	private String newEmail;

	public UserDTO(String token, Long id, String username, String email, String password, boolean isEnabled,
			boolean confirmNewEmail, String newEmail) {
		this.token = token;
		this.id = id;
		this.username = username;
		this.email = email;
		this.password = password;
		this.isEnabled = isEnabled;
		this.confirmNewEmail = confirmNewEmail;
		this.newEmail = newEmail;
	}

	public String getAccessToken() {
		return token;
	}

	public void setAccessToken(String accessToken) {
		this.token = accessToken;
	}

	public boolean isConfirmNewEmail() {
		return confirmNewEmail;
	}

	public void setConfirmNewEmail(boolean confirmNewEmail) {
		this.confirmNewEmail = confirmNewEmail;
	}

	public String getNewEmail() {
		return newEmail;
	}

	public void setNewEmail(String newEmail) {
		this.newEmail = newEmail;
	}

}
