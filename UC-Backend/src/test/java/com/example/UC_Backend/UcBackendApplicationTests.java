package com.example.UC_Backend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * Smoke test — verifies the entire Spring Boot application context loads
 * successfully with a real MongoDB instance (Testcontainers).
 *
 * This test will FAIL if:
 *   - Any @Component/@Service/@Repository has a wiring issue
 *   - application.properties has a syntax error
 *   - A required bean is missing
 *   - MongoDB schema validation fails
 *
 * It's the single most important test in the suite — if this fails,
 * nothing else matters.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = MongoTestContainer.Initializer.class)
@DisplayName("Application Context Smoke Test")
class UcBackendApplicationTests {

    @Test
    @DisplayName("Spring application context loads without errors")
    void contextLoads() {
        // If we get here, all beans were wired successfully
        assertThatNoException().isThrownBy(() -> {});
    }
}
