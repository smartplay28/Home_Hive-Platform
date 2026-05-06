package com.example.UC_Backend.HelperFunctionIO.rejectOrder;

/**
 * This class is used to recieve from frontend when service agent accepts order
 */

public class rejectOrderRequest {
    private int agentId;
    private int orderId;
    private String itemId;

    public rejectOrderRequest() {
    }

    public rejectOrderRequest(int agentId, int orderId, String itemId) {
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

    public void getItemId(String itemId) {
        this.itemId = itemId;
    }

}