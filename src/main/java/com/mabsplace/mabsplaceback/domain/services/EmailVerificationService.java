package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.email.EmailRequest;
import com.mabsplace.mabsplaceback.domain.entities.User;
import com.mabsplace.mabsplaceback.domain.repositories.UserRepository;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailVerificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailVerificationService.class);

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final Map<String, VerificationEntry> verificationCodes = new ConcurrentHashMap<>();

    @Value("${verification.code.expiration.minutes:15}")
    private long expirationMinutes;

    public EmailVerificationService(EmailService emailService, UserRepository userRepository) {
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    public String generateVerificationCode() {
        String code = String.format("%06d", new Random().nextInt(999999));
        logger.debug("Generated verification code: {}", code);
        return code;
    }

    public void sendVerificationCodeToNewEmail(String email) throws MessagingException {
        logger.info("Attempting to send verification code to new email: {}", email);
        String code = generateVerificationCode();
        verificationCodes.put(email, new VerificationEntry(code, Instant.now()));
        logger.debug("Verification code stored for email: {}", email);

        EmailRequest emailRequest = EmailRequest.builder()
                .to(email)
                .subject("MabsPlace Verification Code")
                .headerText("MabsPlace Verification Code")
                .body("<h1>Your verification code is: " + code + "</h1>")
                .companyName("MabsPlace")
                .build();

        emailService.sendEmail(emailRequest);
        logger.info("Verification code sent successfully to email: {}", email);
    }

    public void sendVerificationCode(String email) throws MessagingException {
        logger.info("Attempting to send verification code to email: {}", email);
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            logger.warn("Email not found in database: {}", email);
            throw new MessagingException("Email not found in the database.");
        }

        String code = generateVerificationCode();
        verificationCodes.put(email, new VerificationEntry(code, Instant.now()));
        logger.debug("Verification code stored for email: {}", email);

        EmailRequest emailRequest = EmailRequest.builder()
                .to(email)
                .subject("MabsPlace Verification Code")
                .headerText("MabsPlace Verification Code")
                .body("<h1>Your verification code is: " + code + "</h1>")
                .companyName("MabsPlace")
                .build();

        emailService.sendEmail(emailRequest);
        logger.info("Verification code sent successfully to email: {}", email);
    }

    public boolean verifyCode(String userEmail, String userEnteredCode) {
        logger.info("Verifying code for email: {}", userEmail);
        VerificationEntry entry = verificationCodes.get(userEmail);
        if (entry == null) {
            logger.warn("No verification code found for email: {}", userEmail);
            return false;
        }

        if (isCodeExpired(entry.timestamp())) {
            verificationCodes.remove(userEmail);
            logger.warn("Verification code expired for email: {}", userEmail);
            return false;
        }

        boolean isValid = entry.code().equals(userEnteredCode);
        if (isValid) {
            logger.info("Verification code validated successfully for email: {}", userEmail);
        } else {
            logger.warn("Invalid verification code entered for email: {}", userEmail);
        }
        return isValid;
    }

    private boolean isCodeExpired(Instant timestamp) {
        return Instant.now().isAfter(timestamp.plusSeconds(expirationMinutes * 60));
    }

    @Scheduled(fixedRate = 900000) // Runs every 15 minutes
    private void cleanupExpiredCodes() {
        logger.info("Starting scheduled cleanup of expired verification codes");
        verificationCodes.entrySet().removeIf(entry -> isCodeExpired(entry.getValue().timestamp()));
        logger.info("Completed scheduled cleanup of expired verification codes");
    }

    private record VerificationEntry(String code, Instant timestamp) {}
}