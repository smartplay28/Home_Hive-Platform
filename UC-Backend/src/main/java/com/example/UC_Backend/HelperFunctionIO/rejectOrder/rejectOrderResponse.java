package com.example.UC_Backend.HelperFunctionIO.rejectOrder;

/**
 * For giving back response to the frontend in json format
 */
public class rejectOrderResponse {
    private String message;

    public rejectOrderResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
