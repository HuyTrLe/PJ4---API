package com.mytech.api.auth.password;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mytech.api.auth.email.EmailSender;
import com.mytech.api.auth.payload.request.EmailValidator;
import com.mytech.api.auth.payload.response.MessageResponse;
import com.mytech.api.auth.services.UserDetailServiceImpl;
import com.mytech.api.models.user.User;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ForgotPasswordService {
	@Autowired
	UserDetailServiceImpl userDetailServiceImpl;

	@Autowired
	EmailValidator emailValidator;

	@Autowired
	PasswordResetTokenService passwordResetTokenService;

	@Autowired
	EmailSender emailSender;

	public ResponseEntity<?> forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
		List<String> errors = new ArrayList<>();
		String email = forgotPasswordRequest.getEmail();
		User user = userDetailServiceImpl.findByEmail(email);
		if (user == null) {
			errors.add("Email not found.");
		}
		if (!errors.isEmpty()) {
			List<String> formattedErrors = new ArrayList<>();
			for (String error : errors) {
				formattedErrors.add(error);
			}
			System.out.println(formattedErrors);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(formattedErrors);
		}

		String token = userDetailServiceImpl.forgotPassword(user);
		String link = "http://localhost:3000/auth/reset-password/" + token;
		emailSender.send(email, buildEmail(user.getUsername(), link));
		System.out.println("Please check your email to verify your account.");
		MessageResponse response = new MessageResponse("Please check your email to reset password.");
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@Transactional
	public ResponseEntity<?> resetPassword(String token, String password) {
	    PasswordResetToken passwordResetToken = passwordResetTokenService.getToken(token)
	            .orElseThrow(() -> new IllegalStateException("token not found"));

	    User user = passwordResetToken.getUser();
	    String oldPassword = userDetailServiceImpl.getUserPasswordByResetToken(token);	
	    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
	    if (encoder.matches(password, oldPassword)) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                .body("New password must be different from the old password.");
	    }
	    user.setPassword(new BCryptPasswordEncoder().encode(password));
	    passwordResetTokenService.setConfirmedAt(token);
	    userDetailServiceImpl.save(user);
	    passwordResetTokenService.deleteTokenByTokenValue(token);
	    MessageResponse response = new MessageResponse("Please login again!");
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	private String buildEmail(String name, String link) {
		return "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n" + "\n"
				+ "<span style=\"display:none;font-size:1px;color:#fff;max-height:0\"></span>\n" + "\n"
				+ "  <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;min-width:100%;width:100%!important\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n"
				+ "    <tbody><tr>\n" + "      <td width=\"100%\" height=\"53\" bgcolor=\"#0b0c0c\">\n" + "        \n"
				+ "        <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;max-width:580px\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\">\n"
				+ "          <tbody><tr>\n" + "            <td width=\"70\" bgcolor=\"#0b0c0c\" valign=\"middle\">\n"
				+ "                <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n"
				+ "                  <tbody><tr>\n" + "                    <td style=\"padding-left:10px\">\n"
				+ "                  \n" + "                    </td>\n"
				+ "                    <td style=\"font-size:28px;line-height:1.315789474;Margin-top:4px;padding-left:10px\">\n"
				+ "                      <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\">Reset Password</span>\n"
				+ "                    </td>\n" + "                  </tr>\n" + "                </tbody></table>\n"
				+ "              </a>\n" + "            </td>\n" + "          </tr>\n" + "        </tbody></table>\n"
				+ "        \n" + "      </td>\n" + "    </tr>\n" + "  </tbody></table>\n"
				+ "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n"
				+ "    <tbody><tr>\n" + "      <td width=\"10\" height=\"10\" valign=\"middle\"></td>\n"
				+ "      <td>\n" + "        \n"
				+ "                <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n"
				+ "                  <tbody><tr>\n"
				+ "                    <td bgcolor=\"#1D70B8\" width=\"100%\" height=\"10\"></td>\n"
				+ "                  </tr>\n" + "                </tbody></table>\n" + "        \n" + "      </td>\n"
				+ "      <td width=\"10\" valign=\"middle\" height=\"10\"></td>\n" + "    </tr>\n"
				+ "  </tbody></table>\n" + "\n" + "\n" + "\n"
				+ "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n"
				+ "    <tbody><tr>\n" + "      <td height=\"30\"><br></td>\n" + "    </tr>\n" + "    <tr>\n"
				+ "      <td width=\"10\" valign=\"middle\"><br></td>\n"
				+ "      <td style=\"font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:1.315789474;max-width:560px\">\n"
				+ "        \n"
				+ "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Hi " + name
				+ ",</p><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> Thank you for using us. Please click on the below link to reset your password: </p><blockquote style=\"Margin:0 0 20px 0;border-left:10px solid #b1b4b6;padding:15px 0 0.1px 15px;font-size:19px;line-height:25px\"><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> <a href=\""
				+ link + "\">Activate Now</a> </p></blockquote>\n Link will expire in 15 minutes. <p>See you soon</p>"
				+ "        \n" + "      </td>\n" + "      <td width=\"10\" valign=\"middle\"><br></td>\n"
				+ "    </tr>\n" + "    <tr>\n" + "      <td height=\"30\"><br></td>\n" + "    </tr>\n"
				+ "  </tbody></table><div class=\"yj6qo\"></div><div class=\"adL\">\n" + "\n" + "</div></div>";

	}
}
