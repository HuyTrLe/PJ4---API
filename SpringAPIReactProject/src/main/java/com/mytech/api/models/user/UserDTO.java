package com.mytech.api.models.user;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class UserDTO {
	private Long id;
	private String username;
	private String email;
	private String password;
	private boolean isEnabled;

	public UserDTO(Long id, String username, String email, String password, boolean isEnabled) {
		this.id = id;
		this.username = username;
		this.email = email;
		this.password = password;
		this.isEnabled = isEnabled;		
	}

}
