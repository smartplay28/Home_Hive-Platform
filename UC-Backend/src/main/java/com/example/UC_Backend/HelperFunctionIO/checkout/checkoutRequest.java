package com.example.UC_Backend.HelperFunctionIO.checkout;

/**
 * This file takes care of json file coming when checkout button is clicked
 */

public class checkoutRequest {
    private int customerId;
    private int totalprice;
    private String location;

    public checkoutRequest() {
    }

    public checkoutRequest(int customerId, int totalprice, String location) {
        this.customerId = customerId;
        this.totalprice = totalprice;
        this.location = location;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getTotalprice() {
        return totalprice;
    }

    public void setTotalprice(int totalprice) {
        this.totalprice = totalprice;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}
