package com.mytech.api.auth.UpdateUser.Password;

import java.util.Random;
import java.util.concurrent.*;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.HashMapChangeSet;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mytech.api.auth.UpdateUser.Password.DTO.PasswordChangeRequestDTO;
import com.mytech.api.auth.UpdateUser.Password.DTO.PasswordDTO;
import com.mytech.api.auth.UpdateUser.Password.DTO.VerifyOTPDTO;
import com.mytech.api.auth.email.EmailSenderPin;
import com.mytech.api.auth.repositories.UserRepository;
import com.mytech.api.models.user.User;

@Service
public class UpdatePasswordService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    EmailSenderPin emailSenderPin;

    @Autowired
    PasswordEncoder encoder;

    private ConcurrentHashMap<String, PinData> otpMap = new ConcurrentHashMap<>();

    private static class PinData {
        private String pin;
        private long expiryTime;

        public PinData(String pin, long expiryTime) {
            this.pin = pin;
            this.expiryTime = expiryTime;
        }

        public String getPin() {
            return pin;
        }

        public long getExpiryTime() {
            return expiryTime;
        }
    }

    public ResponseEntity<?> updateUserPassword(PasswordDTO passwordDTO) {
        User user = userRepository.findById(passwordDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!validateNewPassword(passwordDTO.getNewPassword(), passwordDTO.getConfirmNewPassword())) {
            return ResponseEntity.badRequest().body("New passwords do not match");
        }

        if (!checkOldPassword(user, passwordDTO.getOldPassword())) {
            return ResponseEntity.badRequest().body("Old password is incorrect");
        }

        if (!validateNewPassword(passwordDTO.getNewPassword(), passwordDTO.getOldPassword())) {
                
            return ResponseEntity.badRequest().body("New passwords do not match or the new password is the same as the old one");
        }

        String pin = generateAndStoreOTP(user.getEmail());
        emailSenderPin.send(user.getEmail(), buildSend(user.getUsername(), pin));

        return ResponseEntity.ok("OTP sent to email");
    }

    public ResponseEntity<?> changePasswordWithOTP(PasswordChangeRequestDTO requestDTO) {
        PasswordDTO passwordDTO = requestDTO.getPasswordDTO();
        VerifyOTPDTO verifyOTPDTO = requestDTO.getVerifyOTPDTO();

        if (!checkOTP(verifyOTPDTO.getEmail(), verifyOTPDTO.getPin())) {
            return ResponseEntity.badRequest().body("Invalid OTP");
        }

        User user = userRepository.findById(passwordDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String newEncodedPassword = encoder.encode(passwordDTO.getNewPassword());
        user.setPassword(newEncodedPassword);
        userRepository.save(user);

        return ResponseEntity.ok("Password updated successfully");
    }

    private boolean checkOldPassword(User user, String oldPassword) {
        return encoder.matches(oldPassword, user.getPassword());
    }

    private boolean validateNewPassword(String newPassword, String confirmPassword) {
        return newPassword.equals(confirmPassword);
    }

    private String generateAndStoreOTP(String email) {
        Random random = new Random();
        int pin = random.nextInt(90000) + 10000;
        String otp = String.valueOf(pin);

        long expiryTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5);
        otpMap.put(email, new PinData(otp, expiryTime));

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(() -> otpMap.remove(email), 5, TimeUnit.MINUTES);
        executorService.shutdown();

        return otp;
    }

    private boolean checkOTP(String email, String enteredOTP) {
        PinData pinData = otpMap.get(email);

        if (pinData == null) {
            return false;
        }

        if (System.currentTimeMillis() > pinData.getExpiryTime()) {
            otpMap.remove(email);
            return false;
        }

        if (!enteredOTP.equals(pinData.getPin())) {
            return false;
        }

        return true;
    }

    private String buildSend(String name, String pin) {
        return "<div style=\"font-family: Helvetica,Arial,sans-serif;min-width:1000px;overflow:auto;line-height:2\">" +
                "  <div style=\"margin:50px auto;width:70%;padding:20px 0\">" +
                "    <div style=\"border-bottom:1px solid #eee\">" +
                "      <a href=\"\" style=\"font-size:1.4em;color: #00466a;text-decoration:none;font-weight:600\">SEND OTP</a>"
                +
                "    </div>" +
                "    <p style=\"font-size:1.1em\">Hi " + name + ",</p>" +
                "    <p>Thank you for using Manage Money App . Use the following OTP to complete your Update Profile procedures.</p>"
                +
                "    <h2 style=\"background: #00466a;margin: 0 auto;width: max-content;padding: 0 10px;color: #fff;border-radius: 4px;\">"
                + pin + "</h2>" +
                "    <p style=\"font-size:0.9em;\">Regards,<br />Your Brand</p>" +
                "    <hr style=\"border:none;border-top:1px solid #eee\" />" +
                "    <div style=\"float:right;padding:8px 0;color:#aaa;font-size:0.8em;line-height:1;font-weight:300\">"
                +
                "      <p>Your Brand Inc</p>" +
                "      <p>1600 Amphitheatre Parkway</p>" +
                "      <p>California</p>" +
                "    </div>" +
                "  </div>" +
                "</div>";
    }
}
