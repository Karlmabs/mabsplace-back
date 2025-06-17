package com.mabsplace.mabsplaceback.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.mock;

/**
 * Test configuration class that provides mock beans and test-specific configurations
 * for the test environment.
 */
@TestConfiguration
@Profile("test")
public class TestConfig {

    /**
     * Mock JavaMailSender to avoid actual email sending during tests
     */
    @Bean
    @Primary
    public JavaMailSender mockJavaMailSender() {
        return mock(JavaMailSender.class);
    }

    /**
     * Real password encoder for testing authentication flows
     */
    @Bean
    @Primary
    public PasswordEncoder testPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
