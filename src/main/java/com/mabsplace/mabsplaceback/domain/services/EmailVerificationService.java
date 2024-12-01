package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.email.EmailRequest;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailVerificationService {

    private final EmailService emailService;
    private final Map<String, VerificationEntry> verificationCodes = new ConcurrentHashMap<>();

    @Value("${verification.code.expiration.minutes:15}")
    private long expirationMinutes;

    public EmailVerificationService(EmailService emailService) {
        this.emailService = emailService;
    }

    public String generateVerificationCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    public void sendVerificationCode(String email) throws MessagingException {
        String code = generateVerificationCode();
        verificationCodes.put(email, new VerificationEntry(code, Instant.now()));

        EmailRequest emailRequest = EmailRequest.builder()
                .to(email)
                .subject("MabsPlace Verification Code")
                .headerText("MabsPlace Verification Code")
                .body("<h1>Your verification code is: " + code + "</h1>")
                .companyName("MabsPlace")
                .build();

        emailService.sendEmail(emailRequest);
    }

    public boolean verifyCode(String userEmail, String userEnteredCode) {
        VerificationEntry entry = verificationCodes.get(userEmail);
        if (entry == null) {
            return false;
        }

        if (isCodeExpired(entry.timestamp())) {
            verificationCodes.remove(userEmail);
            return false;
        }

        return entry.code().equals(userEnteredCode);
    }

    private boolean isCodeExpired(Instant timestamp) {
        return Instant.now().isAfter(timestamp.plusSeconds(expirationMinutes * 60));
    }

    @Scheduled(fixedRate = 900000) // Runs every 15 minutes
    private void cleanupExpiredCodes() {
        verificationCodes.entrySet().removeIf(entry ->
                isCodeExpired(entry.getValue().timestamp()));
    }

    private record VerificationEntry(String code, Instant timestamp) {}
}