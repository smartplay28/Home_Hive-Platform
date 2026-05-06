package com.example.UC_Backend.Database;

import com.example.UC_Backend.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Order.
 * Compound indexes on (customerId, orderStatus) and (status, createdAt)
 * make all queries here O(log n) instead of O(n) full collection scans.
 */
@Repository
public interface OrderRepository extends MongoRepository<Order, String> {

    Optional<Order> findByOrderId(int orderId);

    /** Uses (customerId, orderStatus) compound index */
    ArrayList<Order> findByCustomerId(int customerId);

    /** Active orders for a customer — uses compound index, very fast */
    List<Order> findByCustomerIdAndOrderStatus(int customerId, String orderStatus);

    /** Admin dashboard: all pending orders sorted newest first — uses (status, createdAt) index */
    List<Order> findByOrderStatusOrderByCreatedAtDesc(String orderStatus);

    /** Count active orders in a location — for demand forecasting */
    long countByLocationAndOrderStatus(String location, String orderStatus);
}