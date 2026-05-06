package com.example.UC_Backend.HelperFunctionIO.loginCustomer;

/**
 * For giving back response to the frontend in json format
 */
public class loginCustomerResponse {
    private String message;
    private int customerId;

    public loginCustomerResponse(String message, int customerId) {
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