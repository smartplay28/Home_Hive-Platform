package com.example.UC_Backend.HelperFunctionIO.checkout;

/**
 * This is response file when an add is added to cart
 * It will just send a message
 */

public class checkoutResponse {
    private String message;

    public checkoutResponse(String message) {
        this.message = message;

    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}