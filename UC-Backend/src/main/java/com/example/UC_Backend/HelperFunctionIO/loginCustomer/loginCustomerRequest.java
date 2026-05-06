package com.example.UC_Backend.HelperFunctionIO.loginCustomer;

/**
 * For taking input from the frontend in json format and converting it into java
 * object of this class.
 */

public class loginCustomerRequest {
    private String email;
    private String password;

    loginCustomerRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}