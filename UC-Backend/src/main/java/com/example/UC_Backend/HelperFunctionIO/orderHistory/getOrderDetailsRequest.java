package com.example.UC_Backend.HelperFunctionIO.orderHistory;

/**
 * This file takes care of json file coming when checkout button is clicked
 */
public class getOrderDetailsRequest {
    private int customerId;

    public getOrderDetailsRequest() {
    }

    public getOrderDetailsRequest(int customerId) {
        this.customerId = customerId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

}
