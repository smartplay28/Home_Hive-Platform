package com.example.UC_Backend.controller;

import com.example.UC_Backend.Database.CustomerRepository;
import com.example.UC_Backend.Database.ServiceAgentRepository;
import com.example.UC_Backend.Order;
import com.example.UC_Backend.Users.Customer;
import com.example.UC_Backend.common.ApiResponse;
import com.example.UC_Backend.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock private OrderService orderService;
    @Mock private CustomerRepository customerRepo;
    @Mock private ServiceAgentRepository agentRepo;

    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    void setUp() {}

    @Test
    void checkout_Success() {
        Map<String, Object> req = new HashMap<>();
        req.put("customerId", 123);
        req.put("totalprice", 500);
        req.put("location", "Test Location");

        Order mockOrder = new Order(123, "Pending", 500, "Test Location");
        mockOrder.setOrderId(999);

        when(orderService.createOrder(123, 500, "Test Location")).thenReturn(mockOrder);
        doNothing().when(orderService).assignAgents(mockOrder);

        ResponseEntity<ApiResponse<Map<String, Integer>>> response = orderController.checkout(req);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(999, response.getBody().data().get("orderId"));
        verify(orderService, times(1)).assignAgents(mockOrder);
    }

    @Test
    void getOrderHistory_Success() {
        Map<String, Integer> req = new HashMap<>();
        req.put("customerId", 123);

        List<Order> mockOrders = new ArrayList<>();
        mockOrders.add(new Order());
        
        when(orderService.getOrderHistory(123)).thenReturn(mockOrders);

        ResponseEntity<ApiResponse<List<Order>>> response = orderController.getOrderHistory(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().data().size());
    }

    @Test
    void acceptOrder_Success() {
        Map<String, Object> req = new HashMap<>();
        req.put("orderId", 999);
        req.put("agentId", 456);
        req.put("itemId", "item1");

        doNothing().when(orderService).acceptOrder(999, 456, "item1");

        ResponseEntity<ApiResponse<Void>> response = orderController.acceptOrder(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Order accepted successfully.", response.getBody().message());
    }

    @Test
    void getCartItems_Success() {
        Customer customer = new Customer("Test", "test@example.com", 1234567890, "password");
        ArrayList<String> cart = new ArrayList<>();
        cart.add("service1");
        customer.setShoppingCart(cart);

        when(customerRepo.findByCustomerId(123)).thenReturn(Optional.of(customer));

        ResponseEntity<ApiResponse<Map<String, List<String>>>> response = orderController.getCartItems(123);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().data().get("cartItems").contains("service1"));
    }

    @Test
    void addToCart_Success() {
        Map<String, Object> req = new HashMap<>();
        req.put("customerId", 123);
        req.put("serviceId", "service1");

        Customer customer = new Customer("Test", "test@example.com", 1234567890, "password");
        when(customerRepo.findByCustomerId(123)).thenReturn(Optional.of(customer));

        ResponseEntity<ApiResponse<Void>> response = orderController.addToCart(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(customer.getShoppingCart().contains("service1"));
        verify(customerRepo, times(1)).save(customer);
    }
}
