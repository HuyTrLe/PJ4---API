package com.mytech.api.auth.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class EmailService implements EmailSender {

	  private final static Logger LOGGER = LoggerFactory.getLogger(EmailService.class);
	    private final JavaMailSender javaMailSender;

	    @Override
	    @Async
	    public void send(String to, String email) {
	        try {
	            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
	            MimeMessageHelper helper =
	                    new MimeMessageHelper(mimeMessage, "utf-8");
	            helper.setText(email, true);
	            helper.setTo(to);
	            helper.setSubject("Confirm your information");
	            helper.setFrom("achauu997@gmail.com");
	            javaMailSender.send(mimeMessage);
	        } catch (MessagingException e) {
	            LOGGER.error("Failed To Send Email.", e);
	            throw new IllegalStateException("Failed To Send Email.");
	        }
	    }
}
