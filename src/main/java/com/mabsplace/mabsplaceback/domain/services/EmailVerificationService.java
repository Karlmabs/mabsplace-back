package com.mabsplace.mabsplaceback.domain.services;

import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class EmailVerificationService {

    private final EmailService emailService;
    private final Map<String, String> verificationCodes = new HashMap<>();

    public EmailVerificationService(EmailService emailService) {
        this.emailService = emailService;
    }

    public String generateVerificationCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    public void sendVerificationCode(String email) throws MessagingException {
        String code = generateVerificationCode();
        verificationCodes.put(email, code);
        emailService.sendEmail(email, "Verification Code", code);
    }

    public boolean verifyCode(String userEmail, String userEnteredCode) {
        String sentCode = verificationCodes.get(userEmail);
        return sentCode != null && sentCode.equals(userEnteredCode);
    }
}