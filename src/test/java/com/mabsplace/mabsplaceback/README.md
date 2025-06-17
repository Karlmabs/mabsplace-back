# Test Suite Documentation

## Overview
This test suite provides comprehensive testing coverage for the MabsPlace Backend application using Spring Boot testing framework.

## Test Structure

### Base Classes
- **BaseControllerTest**: Base class for all controller tests using `@WebMvcTest`
- **BaseServiceTest**: Base class for all service layer tests using `@ExtendWith(MockitoExtension.class)`
- **BaseRepositoryTest**: Base class for all repository tests using `@DataJpaTest`
- **BaseIntegrationTest**: Base class for all integration tests using `@SpringBootTest`

### Test Configuration
- **TestConfig**: Provides mock beans and test-specific configurations
- **TestSecurityConfig**: Disables security for easier testing
- **application.yml**: Test-specific application properties

### Test Utilities
- **TestDataBuilder**: Utility class for creating test data objects with sensible defaults

## Testing Conventions

### Test Method Naming
Follow the pattern: `should_ExpectedBehavior_When_StateUnderTest`

Examples:
- `should_ReturnUser_When_ValidIdProvided()`
- `should_ThrowException_When_UserNotFound()`
- `should_CreatePayment_When_ValidDataProvided()`

### Test Organization
```
src/test/java/com/mabsplace/mabsplaceback/
├── base/                    # Base test classes
├── config/                  # Test configurations
├── utils/                   # Test utilities
├── domain/
│   ├── controllers/         # Controller tests
│   ├── services/           # Service tests
│   ├── repositories/       # Repository tests
│   └── entities/           # Entity tests
├── security/               # Security tests
└── integration/            # Integration tests
```

## Test Types

### 1. Unit Tests
- **Controller Tests**: Test REST endpoints with mocked dependencies
- **Service Tests**: Test business logic with mocked repositories
- **Repository Tests**: Test data access layer with in-memory database

### 2. Integration Tests
- **API Integration Tests**: Test complete request/response cycles
- **Database Integration Tests**: Test with real database interactions
- **Security Integration Tests**: Test authentication and authorization

### 3. Specialized Tests
- **Security Tests**: Test authentication, authorization, and security configurations
- **Configuration Tests**: Verify application properties and bean configurations
- **Performance Tests**: Basic performance tests for critical endpoints

## Running Tests

### All Tests
```bash
mvn test
```

### Specific Test Categories
```bash
# Unit tests only
mvn test -Dtest="**/*Test"

# Integration tests only
mvn test -Dtest="**/*IntegrationTest"

# Controller tests only
mvn test -Dtest="**/controllers/*Test"
```

### Test Coverage
```bash
mvn jacoco:report
```

## Test Data Management

### Using TestDataBuilder
```java
// Create test entities
User testUser = TestDataBuilder.createTestUser();
Payment testPayment = TestDataBuilder.createTestPayment();

// Create test DTOs
UserRequestDto userDto = TestDataBuilder.createUserRequestDto();
PaymentRequestDto paymentDto = TestDataBuilder.createPaymentRequestDto();

// Customize with builder pattern
User customUser = TestDataBuilder.createUserBuilder()
    .username("customuser")
    .email("custom@example.com")
    .build();
```

### Database Cleanup
Tests use `@Transactional` annotation to automatically rollback changes after each test.

## Best Practices

1. **Use appropriate test annotations**: `@WebMvcTest`, `@DataJpaTest`, `@SpringBootTest`
2. **Mock external dependencies**: Use `@MockBean` for Spring beans, `@Mock` for regular objects
3. **Test both positive and negative scenarios**: Happy path and error cases
4. **Use descriptive test names**: Follow the naming convention
5. **Keep tests independent**: Each test should be able to run in isolation
6. **Use test data builders**: Avoid creating test data manually
7. **Verify all assertions**: Test the expected behavior, not just that code runs
8. **Test edge cases**: Null values, empty collections, boundary conditions

## Common Patterns

### Controller Test Example
```java
@WebMvcTest(UserController.class)
class UserControllerTest extends BaseControllerTest {
    
    @MockBean
    private UserService userService;
    
    @Test
    void should_ReturnUser_When_ValidIdProvided() throws Exception {
        // Given
        User user = TestDataBuilder.createTestUser();
        when(userService.findById(1L)).thenReturn(user);
        
        // When & Then
        performGet("/api/users/{id}", 1L)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("testuser"));
    }
}
```

### Service Test Example
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest extends BaseServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    void should_CreateUser_When_ValidDataProvided() {
        // Given
        User user = TestDataBuilder.createTestUser();
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        // When
        User result = userService.createUser(user);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
    }
}
```
