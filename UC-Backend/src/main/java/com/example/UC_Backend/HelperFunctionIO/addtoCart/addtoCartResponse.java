package com.example.UC_Backend.HelperFunctionIO.addtoCart;

/**
 * This is response file when an add is added to cart
 * It will just send a message
 */
public class addtoCartResponse {
    private String message;

    public addtoCartResponse(String message) {
        this.message = message;

    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}