package com.example.UC_Backend.HelperFunctionIO.getCustomerDetails;

/**
 * Accepts customerId from frontend
 */

public class getCustomerDetailsRequest {
    private int customerId;

    public getCustomerDetailsRequest() {
    } // Default constructor for deserialization

    public getCustomerDetailsRequest(int customerId) {
        this.customerId = customerId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) { // Setter for deserialization
        this.customerId = customerId;
    }
}
