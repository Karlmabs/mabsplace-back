package com.mabsplace.mabsplaceback.utils;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class PromoCodeGenerator {
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Removed similar looking characters
    private static final int CODE_LENGTH = 8;

    public String generateCode() {
        StringBuilder code = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));

            // Add hyphen every 4 characters except at the end
            if (i % 4 == 3 && i < CODE_LENGTH - 1) {
                code.append("-");
            }
        }

        return code.toString();
    }
}
