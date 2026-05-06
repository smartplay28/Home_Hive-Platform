package com.example.UC_Backend.HelperFunctionIO.acceptOrder;

/**
 * This class is used to recieve data from frontend when service agent accepts
 * order
 */
public class acceptOrderRequest {
    private int agentId;
    private int orderId;
    private String itemId;

    public acceptOrderRequest() {
    } // Deserialization

    public acceptOrderRequest(int agentId, int orderId, String itemId) {
        this.agentId = agentId;
        this.orderId = orderId;
        this.itemId = itemId;
    }

    public int getAgentId() {
        return this.agentId;
    }

    public void setAgentId(int agentId) {
        this.agentId = agentId;
    }

    public int getOrderId() {
        return this.orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getItemId() {
        return this.itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
}