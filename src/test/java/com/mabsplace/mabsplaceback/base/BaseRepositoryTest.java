package com.mabsplace.mabsplaceback.base;

import com.mabsplace.mabsplaceback.config.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base class for repository tests providing common setup and utilities.
 * All repository tests should extend this class to ensure consistent configuration.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(TestConfig.class)
public abstract class BaseRepositoryTest {

    @Autowired
    protected TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        // Common setup for all repository tests
        setupTestData();
    }

    /**
     * Override this method in subclasses to set up test-specific data
     */
    protected void setupTestData() {
        // Default implementation - can be overridden
    }

    /**
     * Utility method to persist and flush entity
     */
    protected <T> T persistAndFlush(T entity) {
        T persistedEntity = entityManager.persistAndFlush(entity);
        entityManager.clear(); // Clear persistence context to ensure fresh reads
        return persistedEntity;
    }

    /**
     * Utility method to find entity by ID
     */
    protected <T> T findById(Class<T> entityClass, Object id) {
        return entityManager.find(entityClass, id);
    }
}
