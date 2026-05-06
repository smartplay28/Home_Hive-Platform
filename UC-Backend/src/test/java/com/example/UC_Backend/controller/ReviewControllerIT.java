package com.example.UC_Backend.controller;

import com.example.UC_Backend.Database.CustomerRepository;
import com.example.UC_Backend.Database.OrderRepository;
import com.example.UC_Backend.MongoTestContainer;
import com.example.UC_Backend.Order;
import com.example.UC_Backend.Users.Customer;
import com.example.UC_Backend.review.ReviewRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the Review flow.
 *
 * Flow tested:
 *   1. Register customer → get JWT
 *   2. Create an order manually in DB (bypass async assignment)
 *   3. Submit a review → 201
 *   4. Try to submit another review for same order → 409 (duplicate)
 *   5. Fetch agent's reviews → should contain review from step 3
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = MongoTestContainer.Initializer.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Review Controller Integration Tests")
class ReviewControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CustomerRepository customerRepo;
    @Autowired OrderRepository orderRepo;
    @Autowired ReviewRepository reviewRepo;
    @Autowired PasswordEncoder passwordEncoder;

    private static final int AGENT_ID   = 9001;
    private static final int CUSTOMER_ID = 1001;
    private static final int ORDER_ID    = 5001;
    private static String jwtToken;

    @BeforeEach
    void setUp() throws Exception {
        customerRepo.deleteAll();
        orderRepo.deleteAll();
        reviewRepo.deleteAll();

        // Create a test customer with BCrypt password
        Customer customer = new Customer("Review Tester", "reviewer@test.com", 1234567890,
                passwordEncoder.encode("testpass"));
        customerRepo.save(customer);
        int realCustomerId = customer.getCustomerId();

        // Create an order in AGENT_ASSIGNED state (review-eligible)
        Order order = new Order(realCustomerId, "AGENT_ASSIGNED", 800, "Indiranagar");
        order.setOrderId(ORDER_ID);
        order.setAgentId(AGENT_ID);
        orderRepo.save(order);

        // Login to get JWT
        MvcResult loginResult = mockMvc.perform(
                post("/api/v1/auth/customer/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "reviewer@test.com", "password": "testpass" }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        jwtToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .at("/data/accessToken").asText();
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("POST /reviews → 201 Created for valid review")
    void submitReview_success() throws Exception {
        String body = String.format("""
                {
                  "orderId": %d,
                  "agentId": %d,
                  "serviceId": "ACRepair",
                  "rating": 5,
                  "comment": "Excellent punctuality and clean work!",
                  "positiveAspects": ["punctuality", "cleanliness"]
                }
                """, ORDER_ID, AGENT_ID);

        mockMvc.perform(post("/api/v1/reviews")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.rating").value(5))
                .andExpect(jsonPath("$.data.orderId").value(ORDER_ID));

        // Verify persisted in MongoDB
        assertThat(reviewRepo.findByOrderId(ORDER_ID)).isPresent();
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("POST /reviews → 409 Conflict for duplicate review")
    void submitReview_duplicate_returns409() throws Exception {
        String body = String.format("""
                { "orderId": %d, "agentId": %d, "serviceId": "ACRepair", "rating": 4 }
                """, ORDER_ID, AGENT_ID);

        // First review
        mockMvc.perform(post("/api/v1/reviews")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated());

        // Second review — duplicate
        mockMvc.perform(post("/api/v1/reviews")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("already submitted")));
    }

    @Test
    @DisplayName("POST /reviews → 401 without JWT token")
    void submitReview_noToken_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "orderId": 1, "agentId": 1, "serviceId": "x", "rating": 3 }
                        """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /reviews/agents/{id} → returns submitted reviews")
    void getAgentReviews_returnsReviews() throws Exception {
        // Submit a review first
        mockMvc.perform(post("/api/v1/reviews")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format(
                        """
                        { "orderId": %d, "agentId": %d, "serviceId": "ACRepair", "rating": 4 }
                        """, ORDER_ID, AGENT_ID)))
                .andExpect(status().isCreated());

        // Fetch agent's reviews
        mockMvc.perform(get("/api/v1/reviews/agents/" + AGENT_ID)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].agentId").value(AGENT_ID))
                .andExpect(jsonPath("$.data[0].rating").value(4));
    }

    @Test
    @DisplayName("GET /reviews/check/{orderId} → hasReviewed = false before submission")
    void checkReview_beforeSubmit_returnsFalse() throws Exception {
        mockMvc.perform(get("/api/v1/reviews/check/" + ORDER_ID)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasReviewed").value(false));
    }
}
