package com.example.UC_Backend;

import com.example.UC_Backend.Extra.ExtraFunctions;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import java.util.ArrayList;

/**
 * Order document with compound MongoDB indexes for fast query performance.
 *
 * Key indexes:
 *   - (customerId, orderStatus) — powers customer's "active orders" query: O(log n) vs O(n) full scan
 *   - (orderStatus, createdAt)  — powers admin's "pending orders" dashboard sorted by recency
 *   - orderId (unique)          — fast single-order lookup
 *
 * Implements Serializable for Redis cache serialization.
 */
@Document(collection = "orders")
@CompoundIndexes({
    @CompoundIndex(name = "customer_status_idx",  def = "{'customerId': 1, 'orderStatus': 1}"),
    @CompoundIndex(name = "status_created_idx",   def = "{'orderStatus': 1, 'createdAt': -1}"),
    @CompoundIndex(name = "location_status_idx",  def = "{'location': 1, 'orderStatus': 1}")
})
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;
    private ExtraFunctions func = new ExtraFunctions();

    @Id
    private String id = UUID.randomUUID().toString();

    @Indexed(unique = true)
    private int orderId;

    @Indexed
    private int customerId;

    private int agentId;

    @Indexed
    private String orderStatus;

    private ArrayList<String> cart = new ArrayList<>();

    private int totalprice;

    @Indexed
    private String location;

    private ArrayList<Integer> request_agents;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    // Default constructor
    public Order() {
        this.orderId = func.generateID();
    }

    public Order(int customerId, String orderStatus, int totalprice, String location) {
        this.customerId = customerId;
        this.orderId = func.generateID();
        this.orderStatus = orderStatus;
        this.totalprice = totalprice;
        this.location = location;
        request_agents = new ArrayList<>();
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public String getId()                        { return id; }
    public void setId(String id)              { this.id = id; }

    public int getOrderId()                      { return orderId; }
    public void setOrderId(int orderId)          { this.orderId = orderId; }

    public int getCustomerId()                   { return customerId; }
    public void setCustomerId(int customerId)    { this.customerId = customerId; }

    public int getAgentId()                      { return agentId; }
    public void setAgentId(int agentId)          { this.agentId = agentId; }

    public String getOrderStatus()               { return orderStatus; }
    public void setOrderStatus(String status)    { this.orderStatus = status; }

    public ArrayList<String> getCart()           { return cart; }
    public void setCart(ArrayList<String> cart)  { this.cart = cart; }

    public int getTotalPrice()                   { return totalprice; }
    public void setTotalPrice(int totalprice)    { this.totalprice = totalprice; }

    public String getLocation()                  { return location; }
    public void setLocation(String location)     { this.location = location; }

    public ArrayList<Integer> getRequestAgents() { return request_agents; }
    public void setRequestAgents(ArrayList<Integer> request_agents) {
        this.request_agents = request_agents;
    }

    public Instant getCreatedAt()                { return createdAt; }
    public Instant getUpdatedAt()                { return updatedAt; }
}
