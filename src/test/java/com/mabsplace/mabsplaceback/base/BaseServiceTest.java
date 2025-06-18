package com.mabsplace.mabsplaceback.base;

import com.mabsplace.mabsplaceback.config.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for service layer unit tests providing common setup and utilities.
 * All service tests should extend this class to ensure consistent configuration.
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@Import(TestConfig.class)
public abstract class BaseServiceTest {

    @BeforeEach
    void setUp() {
        // Common setup for all service tests
        setupTestData();
        setupMocks();
    }

    /**
     * Override this method in subclasses to set up test-specific data
     */
    protected void setupTestData() {
        // Default implementation - can be overridden
    }

    /**
     * Override this method in subclasses to set up test-specific mocks
     */
    protected void setupMocks() {
        // Default implementation - can be overridden
    }
}
