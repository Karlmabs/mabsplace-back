package com.mabsplace.mabsplaceback.utils;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class PromoCodeGenerator {
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Removed similar looking characters
    private static final int CODE_LENGTH = 8;
    private static final int REFERRAL_CODE_LENGTH = 6;
    
    private final Random random = new Random();

    public String generateCode() {
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));

            // Add hyphen every 4 characters except at the end
            if (i % 4 == 3 && i < CODE_LENGTH - 1) {
                code.append("-");
            }
        }

        return code.toString();
    }
    
    public String generateReferralCode() {
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < REFERRAL_CODE_LENGTH; i++) {
            code.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }

        return code.toString();
    }
}
