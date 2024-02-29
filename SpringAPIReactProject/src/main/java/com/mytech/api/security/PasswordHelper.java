package com.mytech.api.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHelper {

	public static void main(String[] args) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		String encoded =  encoder.encode("Anh");
		System.out.println("PasswordHelper: " + encoded);

	}

}
