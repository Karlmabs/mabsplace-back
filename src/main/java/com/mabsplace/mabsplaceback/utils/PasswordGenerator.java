package com.mabsplace.mabsplaceback.utils;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class PasswordGenerator {
    // Excluded ambiguous characters: 0, O (similar to 0), l, 1, I (similar to 1)
    private static final String UPPERCASE = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghjkmnpqrstuvwxyz";
    private static final String DIGITS = "23456789";
    private static final String SPECIAL = "!@#$%^&*-_=+";
    private static final int PASSWORD_LENGTH = 12;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates a cryptographically secure random password that meets strength requirements.
     * The password will contain at least one character from each category:
     * uppercase, lowercase, digit, and special character.
     *
     * @return A 12-character random password
     */
    public String generateSecurePassword() {
        List<Character> passwordChars = new ArrayList<>();

        // Ensure at least one character from each category
        passwordChars.add(UPPERCASE.charAt(secureRandom.nextInt(UPPERCASE.length())));
        passwordChars.add(LOWERCASE.charAt(secureRandom.nextInt(LOWERCASE.length())));
        passwordChars.add(DIGITS.charAt(secureRandom.nextInt(DIGITS.length())));
        passwordChars.add(SPECIAL.charAt(secureRandom.nextInt(SPECIAL.length())));

        // Combine all character sets for remaining positions
        String allChars = UPPERCASE + LOWERCASE + DIGITS + SPECIAL;

        // Fill remaining positions with random characters from all categories
        for (int i = passwordChars.size(); i < PASSWORD_LENGTH; i++) {
            passwordChars.add(allChars.charAt(secureRandom.nextInt(allChars.length())));
        }

        // Shuffle the characters to ensure random distribution
        Collections.shuffle(passwordChars, secureRandom);

        // Convert list to string
        StringBuilder password = new StringBuilder();
        for (Character c : passwordChars) {
            password.append(c);
        }

        return password.toString();
    }
}
