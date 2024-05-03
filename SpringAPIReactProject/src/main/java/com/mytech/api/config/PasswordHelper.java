package com.mytech.api.config;

import java.util.Base64;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHelper {

	public static void main(String[] args) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		String encoded = encoder.encode("Anh");
		System.out.println("PasswordHelper: " + encoded);

		String secretKey = "BudgetApp";
		String hashedKey = DigestUtils.sha256Hex(secretKey);
		String base64Key = Base64.getEncoder().encodeToString(hashedKey.getBytes());
		System.out.println("Base64 Secret Key: " + base64Key);

	}

}
