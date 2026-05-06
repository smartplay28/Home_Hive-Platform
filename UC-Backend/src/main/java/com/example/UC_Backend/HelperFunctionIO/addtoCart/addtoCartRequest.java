package com.example.UC_Backend.HelperFunctionIO.addtoCart;

/**
 * This class is used to recieve data from frontend when service agent accepts
 * order
 */
public class addtoCartRequest {
    private String serviceId;
    private int customerId;

    public addtoCartRequest() {
    } // Default constructor for deserialization

    public addtoCartRequest(String serviceId, int customerId) {
        this.serviceId = serviceId;
        this.customerId = customerId;
    }

    public int getCustomerId() {
        return customerId;
    }

    // Setter for deserialization
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getServiceId() {
        return this.serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }
}
