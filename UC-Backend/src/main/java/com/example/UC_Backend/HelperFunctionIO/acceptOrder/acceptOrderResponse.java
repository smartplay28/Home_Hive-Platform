package com.example.UC_Backend.HelperFunctionIO.acceptOrder;

/**
 * For giving back response to the frontend in json format
 */
public class acceptOrderResponse {
    private String message;

    public acceptOrderResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
