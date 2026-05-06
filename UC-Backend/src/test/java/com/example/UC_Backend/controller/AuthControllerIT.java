package com.example.UC_Backend.controller;

import com.example.UC_Backend.MongoTestContainer;
import com.example.UC_Backend.Database.CustomerRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController.
 * Uses a real Spring Boot context + real MongoDB (Testcontainers).
 *
 * These tests exercise the FULL stack:
 *   HTTP request → Security filter chain → Controller → Service → Repository → MongoDB
 *
 * Why integration tests on top of unit tests?
 *   Unit tests mock the DB — they can't catch issues like:
 *   - Missing @Indexed annotations causing slow queries
 *   - Wrong MongoDB field mappings
 *   - Security filter not triggering on the right paths
 *   - JWT token being valid but claims being wrong
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = MongoTestContainer.Initializer.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Auth Controller Integration Tests")
class AuthControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CustomerRepository customerRepo;

    private static String accessToken;  // shared across ordered tests

    @BeforeEach
    void cleanDb() {
        customerRepo.deleteAll(); // fresh state for each test
    }

    // ─── Registration ─────────────────────────────────────────────────────────

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("POST /auth/customer/register → 201 Created with JWT")
    void register_success() throws Exception {
        String body = """
                {
                  "name": "Integration Test User",
                  "email": "it@example.com",
                  "phone": 9999999999,
                  "password": "testpass123"
                }
                """;

        MvcResult result = mockMvc.perform(
                post("/api/v1/auth/customer/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.data.email").value("it@example.com"))
                .andReturn();

        // Save token for subsequent tests
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        accessToken = response.at("/data/accessToken").asText();
        assertThat(accessToken).isNotBlank();
    }

    @Test
    @DisplayName("POST /auth/customer/register → 409 for duplicate email")
    void register_duplicateEmail_returns409() throws Exception {
        String body = """
                { "name": "User", "email": "dup@test.com", "phone": 9000000000, "password": "pass123" }
                """;

        // First registration — should succeed
        mockMvc.perform(post("/api/v1/auth/customer/register")
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        // Second with same email — should conflict
        mockMvc.perform(post("/api/v1/auth/customer/register")
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("already exists")));
    }

    @Test
    @DisplayName("POST /auth/customer/register → 400 for missing required fields")
    void register_missingFields_returns400() throws Exception {
        // Missing password
        mockMvc.perform(post("/api/v1/auth/customer/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "name": "User", "email": "bad@test.com" }
                        """))
                .andExpect(status().isBadRequest());
    }

    // ─── Login ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /auth/customer/login → 200 with valid JWT")
    void login_success() throws Exception {
        // Setup: register first
        mockMvc.perform(post("/api/v1/auth/customer/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "name": "Login Test", "email": "login@test.com",
                          "phone": 8888888888, "password": "mypassword" }
                        """))
                .andExpect(status().isCreated());

        // Act: login with same credentials
        mockMvc.perform(post("/api/v1/auth/customer/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "email": "login@test.com", "password": "mypassword" }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.role").value("CUSTOMER"));
    }

    @Test
    @DisplayName("POST /auth/customer/login → 401 for wrong password")
    void login_wrongPassword_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/customer/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "name": "Pass Test", "email": "pass@test.com",
                          "phone": 7777777777, "password": "correct_pass" }
                        """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/customer/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "email": "pass@test.com", "password": "wrong_pass" }
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /auth/customer/login → 401 for unknown email")
    void login_unknownEmail_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/customer/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "email": "ghost@test.com", "password": "anypassword" }
                        """))
                .andExpect(status().isUnauthorized());
    }

    // ─── Security checks ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Protected endpoint returns 401 without JWT")
    void protectedEndpoint_noToken_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
