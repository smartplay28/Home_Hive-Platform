package com.example.UC_Backend.HelperFunctionIO.removeFromCart;

/**
 * Gets data from frontend when customer removes a service from cart
 */

public class removeFromCartRequest {
    private String serviceId;
    private int customerId;

    public removeFromCartRequest() {
    } // Default constructor for deserialization

    public removeFromCartRequest(String serviceId, int customerId) {
        this.serviceId = serviceId;
        this.customerId = customerId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }
}