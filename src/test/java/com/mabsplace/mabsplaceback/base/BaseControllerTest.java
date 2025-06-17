package com.mabsplace.mabsplaceback.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mabsplace.mabsplaceback.config.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * Base class for controller tests providing common setup and utility methods.
 * All controller tests should extend this class to ensure consistent configuration.
 */
@WebMvcTest
@ActiveProfiles("test")
@Import(TestConfig.class)
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Common setup for all controller tests
        setupTestData();
    }

    /**
     * Override this method in subclasses to set up test-specific data
     */
    protected void setupTestData() {
        // Default implementation - can be overridden
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

    /**
     * Utility method to perform GET request
     */
    protected ResultActions performGet(String url, Object... urlVariables) throws Exception {
        return mockMvc.perform(get(url, urlVariables)
                .contentType(MediaType.APPLICATION_JSON));
    }

    /**
     * Utility method to perform POST request with JSON body
     */
    protected ResultActions performPost(String url, Object requestBody) throws Exception {
        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(requestBody)));
    }

    /**
     * Utility method to perform PUT request with JSON body
     */
    protected ResultActions performPut(String url, Object requestBody) throws Exception {
        return mockMvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(requestBody)));
    }

    /**
     * Utility method to perform DELETE request
     */
    protected ResultActions performDelete(String url, Object... urlVariables) throws Exception {
        return mockMvc.perform(delete(url, urlVariables)
                .contentType(MediaType.APPLICATION_JSON));
    }

    /**
     * Utility method to perform authenticated GET request
     */
    protected ResultActions performAuthenticatedGet(String url, String token, Object... urlVariables) throws Exception {
        return mockMvc.perform(get(url, urlVariables)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token));
    }

    /**
     * Utility method to perform authenticated POST request
     */
    protected ResultActions performAuthenticatedPost(String url, String token, Object requestBody) throws Exception {
        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(asJsonString(requestBody)));
    }
}
