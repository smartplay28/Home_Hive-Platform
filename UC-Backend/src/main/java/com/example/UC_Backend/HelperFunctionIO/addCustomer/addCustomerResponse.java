package com.example.UC_Backend.HelperFunctionIO.addCustomer;

/**
 * For giving back response to the frontend in json format
 */
public class addCustomerResponse {
    private String message;
    private int customerId;

    public addCustomerResponse(String message, int customerId) {
        this.message = message;
        this.customerId = customerId;
    }

    public String getMessage() {
        return message;
    }

    public int getCustomerId() {
        return customerId;
    }
}
