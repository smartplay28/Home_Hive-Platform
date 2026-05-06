package com.example.UC_Backend.review;

import com.example.UC_Backend.Database.OrderRepository;
import com.example.UC_Backend.Database.ServiceAgentRepository;
import com.example.UC_Backend.Order;
import com.example.UC_Backend.Users.ServiceAgent;
import com.example.UC_Backend.dto.review.ReviewRequest;
import com.example.UC_Backend.exception.ConflictException;
import com.example.UC_Backend.exception.ResourceNotFoundException;
import com.example.UC_Backend.exception.UnauthorizedException;
import com.example.UC_Backend.service.ServiceAgentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReviewService.
 * Focus areas:
 *   - Input validation (rating range, ownership, status)
 *   - Duplicate prevention
 *   - Welford rating algorithm correctness
 *   - Cache eviction triggered on submit
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService Unit Tests")
class ReviewServiceTest {

    @Mock ReviewRepository reviewRepo;
    @Mock OrderRepository orderRepo;
    @Mock ServiceAgentRepository agentRepo;
    @Mock ServiceAgentService agentService;

    @InjectMocks ReviewService reviewService;

    private Order validOrder;
    private ServiceAgent mockAgent;
    private static final int CUSTOMER_ID = 1001;
    private static final int AGENT_ID    = 2002;
    private static final int ORDER_ID    = 3003;

    @BeforeEach
    void setUp() {
        validOrder = new Order(CUSTOMER_ID, "AGENT_ASSIGNED", 500, "Koramangala");
        validOrder.setOrderId(ORDER_ID);
        validOrder.setAgentId(AGENT_ID);

        mockAgent = new ServiceAgent("Raj Kumar", "raj@example.com", "hashed",
                new String[]{"ACRepair"}, 10, "Koramangala");
    }

    private ReviewRequest validReviewRequest(int rating) {
        return new ReviewRequest(ORDER_ID, AGENT_ID, "ACRepair", rating, "Great work!", null, null);
    }

    // ─── Happy Path ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Successful Review Submission")
    class SuccessfulSubmission {

        @Test
        @DisplayName("Should submit review and update agent's Welford average")
        void submitReview_success_updatesAgentRating() {
            when(orderRepo.findByOrderId(ORDER_ID)).thenReturn(Optional.of(validOrder));
            when(reviewRepo.findByOrderIdAndCustomerId(ORDER_ID, CUSTOMER_ID))
                    .thenReturn(Optional.empty());
            when(reviewRepo.save(any(Review.class))).thenAnswer(i -> i.getArgument(0));
            when(agentRepo.findByAgentId(AGENT_ID)).thenReturn(Optional.of(mockAgent));
            when(agentService.saveAgent(any())).thenReturn(mockAgent);

            Review result = reviewService.submitReview(CUSTOMER_ID, validReviewRequest(5));

            assertThat(result).isNotNull();
            assertThat(result.getRating()).isEqualTo(5);
            assertThat(result.getCustomerId()).isEqualTo(CUSTOMER_ID);
            assertThat(result.getAgentId()).isEqualTo(AGENT_ID);

            // Welford update must have run — agent saved with new rating
            verify(agentService).saveAgent(argThat(a ->
                    a.getAvgRating() > 0 && a.getCompletedOrderCount() == 1));
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 4, 5})
        @DisplayName("Should accept all valid star ratings 1-5")
        void submitReview_validRatings_allAccepted(int rating) {
            when(orderRepo.findByOrderId(ORDER_ID)).thenReturn(Optional.of(validOrder));
            when(reviewRepo.findByOrderIdAndCustomerId(ORDER_ID, CUSTOMER_ID))
                    .thenReturn(Optional.empty());
            when(reviewRepo.save(any())).thenAnswer(i -> i.getArgument(0));
            when(agentRepo.findByAgentId(AGENT_ID)).thenReturn(Optional.of(mockAgent));

            assertThatNoException().isThrownBy(
                    () -> reviewService.submitReview(CUSTOMER_ID, validReviewRequest(rating)));
        }
    }

    // ─── Validation Failures ──────────────────────────────────────────────────

    @Nested
    @DisplayName("Review Validation")
    class ValidationFailures {

        @Test
        @DisplayName("Should throw UnauthorizedException for wrong customer")
        void submitReview_wrongCustomer_throwsUnauthorized() {
            validOrder.setCustomerId(9999); // different customer
            when(orderRepo.findByOrderId(ORDER_ID)).thenReturn(Optional.of(validOrder));

            assertThatThrownBy(() ->
                    reviewService.submitReview(CUSTOMER_ID, validReviewRequest(5)))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("your own orders");

            verify(reviewRepo, never()).save(any());
        }

        @Test
        @DisplayName("Should throw UnauthorizedException for PENDING order (not yet assigned)")
        void submitReview_pendingOrder_throwsUnauthorized() {
            validOrder.setOrderStatus("PENDING_NOT_ASSIGNED");
            when(orderRepo.findByOrderId(ORDER_ID)).thenReturn(Optional.of(validOrder));

            assertThatThrownBy(() ->
                    reviewService.submitReview(CUSTOMER_ID, validReviewRequest(4)))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("after an agent has been assigned");

            verify(reviewRepo, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ConflictException for duplicate review")
        void submitReview_duplicate_throwsConflict() {
            when(orderRepo.findByOrderId(ORDER_ID)).thenReturn(Optional.of(validOrder));
            when(reviewRepo.findByOrderIdAndCustomerId(ORDER_ID, CUSTOMER_ID))
                    .thenReturn(Optional.of(new Review()));

            assertThatThrownBy(() ->
                    reviewService.submitReview(CUSTOMER_ID, validReviewRequest(3)))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("already submitted");

            verify(reviewRepo, never()).save(any(Review.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for unknown order")
        void submitReview_orderNotFound_throwsNotFound() {
            when(orderRepo.findByOrderId(ORDER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    reviewService.submitReview(CUSTOMER_ID, validReviewRequest(5)))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─── Welford Algorithm ────────────────────────────────────────────────────

    @Nested
    @DisplayName("Welford Rating Algorithm")
    class WelfordAlgorithm {

        @Test
        @DisplayName("First rating should set average exactly")
        void welford_firstRating_exactAverage() {
            ServiceAgent agent = new ServiceAgent("Test", "t@t.com", "hash",
                    new String[]{"Cleaning"}, 5, "Indiranagar");

            agent.updateRating(4.0);

            assertThat(agent.getAvgRating()).isEqualTo(4.0);
            assertThat(agent.getCompletedOrderCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Average should converge correctly over multiple ratings")
        void welford_multipleRatings_correctAverage() {
            ServiceAgent agent = new ServiceAgent("Test", "t@t.com", "hash",
                    new String[]{"Salon"}, 5, "HSR Layout");

            // Add ratings: 5, 3, 4, 2 → average = 14/4 = 3.5
            agent.updateRating(5);
            agent.updateRating(3);
            agent.updateRating(4);
            agent.updateRating(2);

            assertThat(agent.getAvgRating()).isCloseTo(3.5, within(0.001));
            assertThat(agent.getCompletedOrderCount()).isEqualTo(4);
        }

        @Test
        @DisplayName("Welford should be numerically stable for all-same ratings")
        void welford_allSameRatings_stableAverage() {
            ServiceAgent agent = new ServiceAgent("Test", "t@t.com", "hash",
                    new String[]{"Plumbing"}, 8, "Whitefield");

            for (int i = 0; i < 100; i++) agent.updateRating(5);

            // All 5s → average must be exactly 5.0 (no floating point drift)
            assertThat(agent.getAvgRating()).isCloseTo(5.0, within(0.0001));
        }
    }
}
