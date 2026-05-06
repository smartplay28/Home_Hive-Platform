package com.example.UC_Backend.HelperFunctionIO.getCustomerDetails;

/**
 * For giving back response to the frontend in json format
 */
public class getCustomerDetailsResponse {
    private String message;
    private String name;
    private String email;
    private int phone;

    public getCustomerDetailsResponse(String message, String name, String email, int phone) {
        this.message = message;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public String getMessage() {
        return message;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public int getPhone() {
        return phone;
    }
}