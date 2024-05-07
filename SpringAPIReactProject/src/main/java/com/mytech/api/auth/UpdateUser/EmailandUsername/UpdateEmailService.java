package com.mytech.api.auth.UpdateUser.EmailandUsername;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mytech.api.auth.UpdateUser.EmailandUsername.DTO.UserProfileDTO;
import com.mytech.api.auth.email.EmailSender;
import com.mytech.api.auth.payload.request.token.emailupdate.EmailUpdateConfirmationToken;
import com.mytech.api.auth.payload.request.token.emailupdate.EmailUpdateConfirmationTokenService;
import com.mytech.api.auth.repositories.UserRepository;
import com.mytech.api.models.user.User;

import jakarta.transaction.Transactional;

@Service
public class UpdateEmailService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    EmailSender emailSender;

    @Autowired
    EmailUpdateConfirmationTokenService emailUpdateConfirmationTokenService;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    PasswordEncoder encoder;

    public ResponseEntity<?> updateUser(UserProfileDTO userDTO) {
        User existingUser = userRepository.findById(userDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String newUsername = userDTO.getUsername();
        String newEmail = userDTO.getEmail();

        // Check if new email is different and already exists
        Optional<User> existingUserWithEmail = userRepository.findByEmail(newEmail);
        if (existingUserWithEmail.isPresent() && !existingUserWithEmail.get().getId().equals(existingUser.getId())) {
            return ResponseEntity.badRequest().body("Email is already taken");
        }

        // Check if new email is different
        if (!newEmail.equals(existingUser.getEmail())) {
            userDTO.setConfirmNewEmail(true);
        }

        // Check if only username is updated
        if (newUsername != null && !newUsername.equals(existingUser.getUsername())
                && newEmail.equals(existingUser.getEmail())) {
            updateUserUsername(existingUser, newUsername);
            return ResponseEntity.ok("Username updated successfully.");
        }

        // Check if email is updated
        if (userDTO.isConfirmNewEmail()) {
            try {
                String token = UUID.randomUUID().toString();
                EmailUpdateConfirmationToken newToken = new EmailUpdateConfirmationToken(token, LocalDateTime.now(),
                        LocalDateTime.now().plusMinutes(15), existingUser);
                newToken.setOldEmail(existingUser.getEmail());
                newToken.setNewEmail(newEmail);
                emailUpdateConfirmationTokenService.save(newToken);

                String link = "http://localhost:8080/api/auth/updateEmailUsernameProfile/confirmToken?token=" + token;
                emailSender.send(newEmail, buildEmail(existingUser.getUsername(), link));
                return ResponseEntity
                        .ok("Email confirmation required. Confirmation link sent to your new email address.");
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to send confirmation email");
            }
        } else {
            // Update email
            updateUserEmail(existingUser.getEmail(), newEmail);
            return ResponseEntity.ok("User updated successfully.");
        }
    }

    private void updateUserUsername(User existingUser, String newUsername) {
        existingUser.setUsername(newUsername);
        userRepository.save(existingUser);
    }

    @Transactional
    public ResponseEntity<String> confirmToken(String token) {
        EmailUpdateConfirmationToken confirmationToken = emailUpdateConfirmationTokenService.getToken(token)
                .orElseThrow(() -> new IllegalStateException("Token not found"));

        if (confirmationToken.getConfirmedAt() != null) {
            String alertScript = "<script>alert('Email already confirmed'); window.location.href='http://localhost:3000/user';</script>";
            return ResponseEntity.ok(alertScript);
        }

        LocalDateTime expiredAt = confirmationToken.getExpiresAt();
        if (expiredAt.isBefore(LocalDateTime.now())) {
            String alertScript = "<script>alert('Token expired'); window.location.href='http://localhost:3000/user';</script>";
            return ResponseEntity.ok(alertScript);
        }

        confirmationToken.setConfirmedAt(LocalDateTime.now());
        emailUpdateConfirmationTokenService.save(confirmationToken);

        String redirectScript = "<script>alert('Email update success');window.location.href='http://localhost:3000/user';</script>";
        updateUserEmail(confirmationToken.getOldEmail(), confirmationToken.getNewEmail());
        return ResponseEntity.ok(redirectScript);
    }

    private void updateUserEmail(String currentEmail, String newEmail) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setEmail(newEmail);
        userRepository.save(user);
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
                + "                      <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\">Confirm your email</span>\n"
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
                + ",</p><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Please click on the below link to confirm your email: </p><blockquote style=\"Margin:0 0 20px 0;border-left:10px solid #b1b4b6;padding:15px 0 0.1px 15px;font-size:19px;line-height:25px\"><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> <a href=\""
                + link + "\">Activate Now</a> </p></blockquote>\n Link will expire in 15 minutes. <p>See you soon</p>"
                + "        \n" + "      </td>\n" + "      <td width=\"10\" valign=\"middle\"><br></td>\n"
                + "    </tr>\n" + "    <tr>\n" + "      <td height=\"30\"><br></td>\n" + "    </tr>\n"
                + "  </tbody></table><div class=\"yj6qo\"></div><div class=\"adL\">\n" + "\n" + "</div></div>";
    }
}
