package com.example.UC_Backend;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

/**
 * Shared Testcontainer infrastructure for all integration tests.
 *
 * Why a shared container?
 *   - Starting a MongoDB container takes ~3s. If each IT class starts its own,
 *     a suite of 10 IT tests would waste 30s just on container startup.
 *   - Sharing one container across all IT tests saves time without losing isolation
 *     (each test class can drop/recreate its collections).
 *
 * Pattern: Spring ApplicationContextInitializer injects the container's dynamic
 * host/port into Spring properties BEFORE the application context starts.
 * This means Spring's @Value("${spring.data.mongodb.uri}") gets the real container URL.
 *
 * Usage in test class:
 *   @SpringBootTest
 *   @ContextConfiguration(initializers = MongoTestContainer.Initializer.class)
 *   @AutoConfigureMockMvc
 *   class MyIT { ... }
 */
public class MongoTestContainer {

    /** Singleton container — started once, reused across all IT tests */
    static final MongoDBContainer MONGO_DB =
            new MongoDBContainer(DockerImageName.parse("mongo:7.0"))
                    .withReuse(true); // Testcontainers reuse flag — survives between test runs in dev

    static {
        Startables.deepStart(MONGO_DB).join();
    }

    /**
     * Injects dynamic MongoDB URI into Spring context.
     * Replaces application.properties mongo URI at test time.
     */
    public static class Initializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext ctx) {
            TestPropertyValues.of(
                    "spring.data.mongodb.uri=" + MONGO_DB.getReplicaSetUrl("urban_crap_test"),
                    // Disable Redis for IT tests — too heavy for unit-like integration tests
                    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"
            ).applyTo(ctx.getEnvironment());
        }
    }
}
