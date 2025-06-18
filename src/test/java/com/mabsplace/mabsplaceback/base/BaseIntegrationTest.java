package com.mabsplace.mabsplaceback.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mabsplace.mabsplaceback.config.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for integration tests providing common setup and utilities.
 * All integration tests should extend this class to ensure consistent configuration.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
public abstract class BaseIntegrationTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        setupTestData();
    }

    /**
     * Override this method in subclasses to set up test-specific data
     */
    protected void setupTestData() {
        // Default implementation - can be overridden
    }

    /**
     * Utility method to create HTTP headers with authentication
     */
    protected HttpHeaders createAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("Content-Type", "application/json");
        return headers;
    }

    /**
     * Utility method to perform authenticated GET request
     */
    protected <T> ResponseEntity<T> performAuthenticatedGet(String url, String token, Class<T> responseType) {
        HttpHeaders headers = createAuthHeaders(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(baseUrl + url, HttpMethod.GET, entity, responseType);
    }

    /**
     * Utility method to perform authenticated POST request
     */
    protected <T> ResponseEntity<T> performAuthenticatedPost(String url, String token, Object requestBody, Class<T> responseType) {
        HttpHeaders headers = createAuthHeaders(token);
        HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);
        return restTemplate.exchange(baseUrl + url, HttpMethod.POST, entity, responseType);
    }

    /**
     * Utility method to convert object to JSON string
     */
    protected String asJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }
}
